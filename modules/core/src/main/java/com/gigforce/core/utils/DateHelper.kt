package com.gigforce.core.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getDateInDDMMYYYY(date: Date) : String{
        return SimpleDateFormat("dd/MM/yyyy").format(date)
    }

    fun getDateInDDMMYYYYHiphen(date: Date) : String{
        return SimpleDateFormat("dd-MM-yyyy").format(date)
    }

    fun getFullDateTimeStamp() : String{
        return SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }

    fun getDateInDDMMMYYYY(date: Date) : String {
        return SimpleDateFormat("dd-MMM-yyyy").format(date)
    }

    fun getDateFromDDMMYYYY(dateStr: String):Date?{
        val format = SimpleDateFormat("dd-MM-yyyy")
        return try {
            val date = format.parse(dateStr)
            date
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun getDateInDDMMMYYYYComma(date: Date): String{
        return SimpleDateFormat("dd MMM, yyyy").format(date)
    }

    fun getDateInYYYYMMDD(date: Date): String{
        return SimpleDateFormat("yyyy-MM-dd").format(date)
    }

    fun getDateFromString(input: String): Date?{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        var d: Date? = null
        try {
            d = dateFormat.parse(input)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return d
    }


}