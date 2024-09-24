package no.nav.hjelpemidler.medlemskap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.http.openid.TokenSet
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class LovmeApiClientTest {
    private val objectMapper = jacksonObjectMapper()
    private suspend fun <T> test(
        body: T,
        status: HttpStatusCode = HttpStatusCode.OK,
        block: suspend LovMeApiClient.() -> Unit,
    ) {
        val client = LovMeApiClient(
            baseUrl = "http://lovme/api",
            engine = MockEngine {
                val headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                when {
                    it.url.encodedPath.endsWith("/token") -> respond(
                        content = objectMapper.writeValueAsString(
                            TokenSet.bearer(
                                1.hours,
                                "token"
                            )
                        ),
                        headers = headers
                    )

                    else -> respond(
                        content = jsonMapper.writeValueAsString(body),
                        status = status,
                        headers = headers
                    )
                }
            },
        )
        block(client)
    }

    @Test
    fun `skal vurdere medlemskap`() = runTest {
        test(
            VurderMedlemskapDto(
                status = MedlemskapStatus.JA,
                detaljer = null
            )
        ) {
            val vurderMedlemskapDto = vurderMedlemskap(
                søknadID = UUID.randomUUID(),
                fnr = Fødselsnummer("15084300133")
            )
            assertEquals(vurderMedlemskapDto?.status, MedlemskapStatus.NEI)
        }
    }
}
