package com.gigforce.core.date

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getDateInDDMMYYYY(date : Date) : String{
        return SimpleDateFormat("dd/MM/yyyy").format(date)
    }

    fun getFullDateTimeStamp() : String{
        return SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }

    fun getHourMinutes(date : Date):String {
        return SimpleDateFormat("hh.mm aa").format(date)
    }
}