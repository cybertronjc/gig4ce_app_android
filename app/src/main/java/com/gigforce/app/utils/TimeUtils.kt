package com.gigforce.app.utils

import java.text.SimpleDateFormat
import java.util.*

fun parseTime(format: String, date: Date?): String {
    return SimpleDateFormat(format).format(date)
}