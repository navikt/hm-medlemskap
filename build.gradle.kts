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
    implementation(libs.hotlibs.http)
    implementation(libs.hotlibs.core)

    testImplementation(libs.bundles.test)
    testImplementation(libs.hotlibs.core)
}

kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }
