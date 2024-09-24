package no.nav.hjelpemidler.medlemskap

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk

internal open class TestResponseException(
    response: HttpResponse,
    cachedResponseText: String,
) : ResponseException(response, cachedResponseText) {
    companion object {
        internal val Conflict = status(HttpStatusCode.Conflict)
        internal val InternalServerError = status(HttpStatusCode.InternalServerError)

        private fun status(status: HttpStatusCode): ResponseException {
            val httpResponse = mockk<HttpResponse>()
            every { httpResponse.status } returns status
            return TestResponseException(httpResponse, status.toString())
        }
    }
}
