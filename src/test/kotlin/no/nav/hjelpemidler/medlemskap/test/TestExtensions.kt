package no.nav.hjelpemidler.medlemskap.test

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import no.nav.hjelpemidler.medlemskap.jsonMapper
import java.nio.file.Path

inline fun <reified T> JsonMapper.readValue(path: Path): T =
    readValue<T>(path.toFile())

suspend fun HttpRequestData.json(): JsonNode =
    jsonMapper.readTree(body.toByteArray())
