package no.nav.hjelpemidler.medlemskap

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.hjelpemidler.medlemskap.mottak.SøknadMottak

fun main() {
    val lovMeApiClient = LovMeApiClient()
    RapidApplication
        .create(no.nav.hjelpemidler.configuration.Configuration.current)
        .apply {
            SøknadMottak(this, lovMeApiClient)
        }
        .start()
}
