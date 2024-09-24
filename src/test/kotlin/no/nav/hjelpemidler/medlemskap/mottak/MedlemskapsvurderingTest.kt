package no.nav.hjelpemidler.medlemskap.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.medlemskap.MedlemskapStatus
import no.nav.hjelpemidler.medlemskap.VurderMedlemskapDto
import no.nav.hjelpemidler.medlemskap.jsonMapper
import no.nav.hjelpemidler.medlemskap.test.respondJson
import no.nav.hjelpemidler.medlemskap.test.testLovMeApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class MedlemskapsvurderingTest {
    private val søknadId = UUID.randomUUID()
    private val fnr = Fødselsnummer("15084300133")
    private val lovMeApiClient = testLovMeApiClient { request ->

        respondJson(
            HttpStatusCode.OK, VurderMedlemskapDto(status = MedlemskapStatus.JA, detaljer = null)
        )
    }
    private val rapid = TestRapid().apply {
        SøknadMottak(this, lovMeApiClient)
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
    }

    @Test
    fun `kaller lovme-api med argumenter fra melding`() {
        // given
        val saksgrunnlag = jsonMapper.createObjectNode().put("foo", "bar")
        // then
        shouldNotThrowAny {
            // when
            rapid.sendTestMessage(søknadId, fnr, saksgrunnlag)
        }
    }

    private fun TestRapid.sendTestMessage(soknadId: UUID, fnr: Fødselsnummer, saksgrunnlag: JsonNode) = sendTestMessage(
        """
            {
              "eventName": "hm-behovsmeldingMottatt",
              "soknadId": "$soknadId",
              "fnrBruker": "$fnr",
              "saksgrunnlag": ${jsonMapper.writeValueAsString(saksgrunnlag)}
            }
        """.trimIndent()
    )
}
