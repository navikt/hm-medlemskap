plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
}

application { mainClass.set("no.nav.hjelpemidler.medlemskap.ApplicationKt") }

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.logging)
    implementation(libs.rapidsAndRivers)
    implementation(libs.hm.http)
    implementation(libs.hm.core)

    testImplementation(libs.bundles.test)
    testImplementation(libs.hm.core)
}

kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }
