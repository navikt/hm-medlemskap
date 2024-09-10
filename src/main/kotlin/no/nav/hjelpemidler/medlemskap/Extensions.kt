package no.nav.hjelpemidler.medlemskap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.treeToValue
import java.util.UUID

val jsonMapper: JsonMapper =
    jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()

fun JsonNode.uuidValue(): UUID = UUID.fromString(textValue())

inline fun <reified T> JsonNode.asObject(): T = jsonMapper.treeToValue(this)
