package no.nav.hjelpemidler.medlemskap.test

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import no.nav.hjelpemidler.configuration.EntraIDEnvironmentVariable.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT
import no.nav.hjelpemidler.http.openid.TokenSet
import no.nav.hjelpemidler.medlemskap.LovMeApiClient
import no.nav.hjelpemidler.medlemskap.jsonMapper
import kotlin.time.Duration.Companion.hours

fun testLovMeApiClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): LovMeApiClient =
    LovMeApiClient(engine = MockEngine { request ->
        when (request.url.toString()) {
            AZURE_OPENID_CONFIG_TOKEN_ENDPOINT -> respondJson(
                HttpStatusCode.OK,
                TokenSet.bearer(1.hours, "token")
            )

            else -> handler(request)
        }
    }, retry = false)

fun <T> MockRequestHandleScope.respondJson(status: HttpStatusCode, value: T): HttpResponseData =
    respond(jsonMapper.writeValueAsString(value), status, headers {
        append("Content-Type", "application/json")
    })
