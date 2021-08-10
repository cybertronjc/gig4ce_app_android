package com.gigforce.core.utils

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getDateInDDMMYYYY(date : Date) : String{
        return SimpleDateFormat("dd/MM/yyyy").format(date)
    }

    fun getDateInDDMMYYYYHiphen(date : Date) : String{
        return SimpleDateFormat("dd-MM-yyyy").format(date)
    }

    fun getFullDateTimeStamp() : String{
        return SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    }

    fun getDateInDDMMMYYYY(date : Date) : String {
        return SimpleDateFormat("dd-MMM-yyyy").format(date)
    }
}