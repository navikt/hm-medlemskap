package no.nav.hjelpemidler.medlemskap.mottak

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.coroutines.withLoggingContextAsync
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.configuration.GcpEnvironment
import no.nav.hjelpemidler.domain.person.toFødselsnummer
import no.nav.hjelpemidler.medlemskap.LovMeApiClient
import no.nav.hjelpemidler.medlemskap.uuidValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID

private val log = KotlinLogging.logger {}
private val skip = emptySet<UUID>()
private val jsonMapper: JsonMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()

class SøknadMottak(
    rapidsConnection: RapidsConnection,
    lovMeApiClient: LovMeApiClient,
) : AsyncListener(lovMeApiClient) {
    init {
        River(rapidsConnection)
            .apply {
                validate {
                    it.demandAny(
                        "eventName",
                        listOf(
                            "hm-søknadMedFullmaktMottatt",
                            "hm-søknadGodkjentAvBrukerMottatt",
                            "hm-behovsmeldingMottatt",
                        ),
                    )
                }
                validate { it.requireKey("fnrBruker", "soknadId") }
            }
            .register(this)
    }

    private val JsonMessage.fnrBruker: String
        get() = get("fnrBruker").textValue()

    override suspend fun onPacketAsync(packet: JsonMessage, context: MessageContext) {
        val eventName = packet["eventName"].asText()
        val søknadID = packet["soknadId"].uuidValue()
        val fnr = packet.fnrBruker.toFødselsnummer()
        if (søknadID in skip) {
            log.info { "Hopper over søknadId: $søknadID" }
            return
        }
        when (Environment.current) {
            GcpEnvironment.PROD -> log.info {
                "Mottok søknadevent $eventName med søknadId: $søknadID"
            }
            // Gjør foreløpig bare medlemskapsvurdering i dev
            else -> withLoggingContextAsync("packet" to packet.toJson()) {
                log.info { "Mottok søknadevent $eventName med søknadId: $søknadID for bruker $fnr" }
                val medlemskapVurdering = lovMeApiClient.vurderMedlemskap(søknadID, fnr)
                val alder = Period.between(fnr.fødselsdato, LocalDate.now()).years

                log.info { "Mottok medlemskapsvurdering: $medlemskapVurdering" }

                if (medlemskapVurdering != null) {
                    log.info { "Svar fra LovMe for søknad $søknadID: ${medlemskapVurdering.status}" }

                    val medlemskapsvurderingHendelse = mapOf(
                        "eventName" to "hm-bigquery-sink-hendelse",
                        "schemaId" to "medlemskap_v1",
                        "payload" to mapOf(
                            "opprettet" to LocalDateTime.now(),
                            "resultat" to medlemskapVurdering.status,
                            "detaljer" to medlemskapVurdering.detaljer,
                            "alder" to alder,

                            ),
                    )


                    val payload = jsonMapper.writeValueAsString(medlemskapsvurderingHendelse)
                    log.info { "Payload til BigQuery $payload" }
                    context.publish(søknadID.toString(), payload)
                }
            }
        }
    }
}
