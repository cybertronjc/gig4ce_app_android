package com.gigforce.common_ui.ext

import java.text.NumberFormat
import java.util.*

fun Double?.formatToCurrency(
    textInCaseOfNull: String? = null,
    defaultCurrencySymbol: String? = null
): String {

    return if (this == null) {
        textInCaseOfNull ?: "-"
    } else {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance("INR")
        format.format(this)
    }
}