package no.nav.hjelpemidler.medlemskap

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.medlemskap.mottak.SøknadMottak

private val log = KotlinLogging.logger {}

fun main() {
    log.info { "Gjeldende miljø: ${Environment.current}" }

    TILLAT_SYNTETISKE_FØDSELSNUMRE = !Environment.current.tier.isProd

    val lovMeApiClient = LovMeApiClient()

    RapidApplication
        .create(no.nav.hjelpemidler.configuration.Configuration.current)
        .apply {
            SøknadMottak(this, lovMeApiClient)
        }
        .start()
}
