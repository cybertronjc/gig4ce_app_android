package com.gigforce.app.modules.auth.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateProvider {
    private const val A_DATE = 1493769600000L
    fun provideSystemLocaleFormattedDate(): String {
        return SimpleDateFormat.getDateInstance(DateFormat.LONG)
            .format(Date(A_DATE))
    }

    fun provideLocaleFormattedDate(locale: Locale?): String {
        return SimpleDateFormat.getDateInstance(DateFormat.LONG, locale)
            .format(Date(A_DATE))
    }
}