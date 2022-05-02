package com.gigforce.app.android_common_utils.extensions

import java.util.*

fun String.capitalizeFirstLetter(): String = this.replaceFirstChar {
    if (it.isLowerCase())
        it.titlecase(Locale.getDefault())
    else
        it.toString()
}
