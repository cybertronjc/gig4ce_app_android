package com.gigforce.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getDateInDDMMYYYY(date : Date) : String{
        return SimpleDateFormat("dd/MM/yyyy").format(date)
    }

    fun getFullDateTimeStamp() : String{
        return SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }
}