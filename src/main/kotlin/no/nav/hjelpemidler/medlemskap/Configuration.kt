package no.nav.hjelpemidler.medlemskap

import no.nav.hjelpemidler.configuration.EnvironmentVariable

object Configuration {
    val LOVME_API_BASE_URL by EnvironmentVariable
    val LOVME_API_CLIENT_SCOPE by EnvironmentVariable
}
