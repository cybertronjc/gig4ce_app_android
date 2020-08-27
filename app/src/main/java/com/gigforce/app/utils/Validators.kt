package com.gigforce.app.utils

import android.util.Patterns

fun isValidMobile(phone: String): Boolean {
    return Patterns.PHONE.matcher(phone).matches() && phone.length == 10
}

fun isValidMail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
