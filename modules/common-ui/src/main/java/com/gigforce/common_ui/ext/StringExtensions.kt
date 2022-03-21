package com.gigforce.common_ui.ext

fun String?.nonEmptyStringOrNull() : String?{
    return if(this.isNullOrBlank()) null else this
}