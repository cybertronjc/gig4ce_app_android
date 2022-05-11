package com.gigforce.app.data.local.room_type_converters

import androidx.room.TypeConverter
import java.time.LocalDateTime

class LocalDateTimeTypeConverter {

    @TypeConverter
    fun toDate(value: String?): LocalDateTime? {
        val nonNullTimeString = value ?: return null
        return LocalDateTime.parse(nonNullTimeString)
    }

    @TypeConverter
    fun toLong(value: LocalDateTime?): String? {
        return value?.toString()
    }
}