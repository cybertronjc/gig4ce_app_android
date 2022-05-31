package com.gigforce.core.retrofit.custom_serialization

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object LocalDateFromIsoStringDateDeserializer : JsonDeserializer<LocalDate?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate? {
        val jsonString = json ?: return null
        return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(jsonString.asString))
    }
}

object LocalDateToIsoStringDateSerializer : JsonSerializer<LocalDate?> {

    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {

        val date = src ?: return JsonNull.INSTANCE
        return JsonPrimitive(
            DateTimeFormatter.ISO_DATE.format(date)
        )
    }
}