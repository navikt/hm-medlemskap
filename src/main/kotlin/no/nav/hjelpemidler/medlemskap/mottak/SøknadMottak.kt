package no.nav.hjelpemidler.medlemskap.mottak

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
import java.util.UUID

private val log = KotlinLogging.logger {}
private val skip = emptySet<UUID>()

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

            else -> withLoggingContextAsync("packet" to packet.toJson()) {
                log.info { "Mottok søknadevent $eventName med søknadId: $søknadID for bruker $fnr" }
            }
        }
        val medlemskapVurdering = lovMeApiClient.vurderMedlemskap(søknadID, fnr)
        if (medlemskapVurdering != null) {
            log.info { medlemskapVurdering }
            log.info { "Svar fra LovMe for søknad $søknadID: ${medlemskapVurdering.status}" }
        }
    }
}
