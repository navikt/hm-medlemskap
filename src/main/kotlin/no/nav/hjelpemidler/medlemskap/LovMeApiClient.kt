package no.nav.hjelpemidler.medlemskap

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.http.correlationId
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.azureAD
import io.ktor.client.plugins.logging.*
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger {}

class LovMeApiClient(
    private val baseUrl: String = Configuration.LOVME_API_BASE_URL,
    private val scope: String = Configuration.LOVME_API_CLIENT_SCOPE,
    engine: HttpClientEngine = CIO.create(),
    retry: Boolean = true,
) {
    private val client: HttpClient = createHttpClient(engine = engine) {
        expectSuccess = false
        if (retry) {
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(5)
                exponentialDelay()
            }
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            correlationId()
        }
        azureAD(scope, engine) {
            cache(leeway = 10.seconds) {
                maximumSize = 1
            }
        }
    }

    suspend fun vurderMedlemskap(søknadID: UUID, fnr: Fødselsnummer): VurderMedlemskapDto? =
        client
            .post("$baseUrl/vurdering") {
                setBody(mapOf("fnr" to fnr.toString()))
            }
            .expectBody("Sendt forespørsel om vurdering av medlemskap for søknad $søknadID")


    private suspend inline fun <reified T> HttpResponse.expectBody(melding: String): T? {

      log.info { "Fikk svar $status" }
       return when {
            status.isSuccess() -> body<T>()
            status == HttpStatusCode.Conflict -> null
            else -> error("$melding, uventet svar fra tjeneste, status: '$status', body: '${bodyAsTextOrNull()}'")
        }
    }

    private suspend fun HttpResponse.expectSuccess(melding: String) {
        when {
            status.isSuccess() -> log.info { "$melding, success" }
            status == HttpStatusCode.Conflict -> log.warn { "$melding, duplikat, svarte: '$status'" }
            else -> error("$melding, uventet svar fra tjeneste, status: '$status', body: '${bodyAsTextOrNull()}'")
        }
    }
}
private suspend fun HttpResponse.bodyAsTextOrNull(): String? = runCatching { bodyAsText() }.getOrNull()

data class VurderMedlemskapDto(
    val status: MedlemskapStatus,
    val detaljer: String?,
)

enum class MedlemskapStatus {
    JA,
    NEI,
    UAVKLART,
}
