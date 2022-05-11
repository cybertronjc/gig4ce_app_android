package com.gigforce.core.date

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtil {

    fun getDateInDDMMYYYY(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy").format(date)
    }

    fun getFullDateTimeStamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }

    fun getHourMinutes(date: Date): String {
        return SimpleDateFormat("hh.mm aa").format(date)
    }

    private fun parseAndGetCurrentZoneDateTime(
        dateTimeString: String
    ): ZonedDateTime {
        val parsedDateTime = LocalDateTime.parse(
            dateTimeString,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )
        return parsedDateTime.atOffset(
            ZoneOffset.UTC
        ).atZoneSameInstant(
            ZoneId.systemDefault()
        )
    }

    fun getDateFromUTCDateTimeString(
        dateTimeString: String
    ): Date {
        val processedTime = parseAndGetCurrentZoneDateTime(dateTimeString)
        return Date.from(processedTime.toInstant())
    }

    fun getDateFromUTCDateTimeStringOrNull(
        dateTimeStr: String?
    ): Date? {
        val dateTimeString = dateTimeStr ?: return null
        return getDateFromUTCDateTimeString(dateTimeString)
    }

    fun getFirebaseTimestampFromUTCDateTimeString(
        dateTime: String
    ): Timestamp {
        return Timestamp(
            getDateFromUTCDateTimeString(dateTime)
        )
    }


}