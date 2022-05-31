package com.gigforce.core.extensions

fun String?.nonEmptyStringOrNull() : String?{
    return if(this.isNullOrBlank()) null else this
}