package no.nav.hjelpemidler.medlemskap

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.hjelpemidler.medlemskap.mottak.SøknadMottak

private val log = KotlinLogging.logger {}

fun main() {
    val lovMeApiClient = LovMeApiClient()

    log.info { "LovMe API Client, starter RapidApplication, ${no.nav.hjelpemidler.configuration.Configuration.current}"  }

    RapidApplication
        .create(no.nav.hjelpemidler.configuration.Configuration.current)
        .apply {
            SøknadMottak(this, lovMeApiClient)
        }
        .start()
}
