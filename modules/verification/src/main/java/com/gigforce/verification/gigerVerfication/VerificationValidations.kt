package com.gigforce.verification.gigerVerfication

import java.util.regex.Pattern

object VerificationValidations {

    //https://en.wikipedia.org/wiki/Permanent_account_number
    private const val PAN_REGEX = "[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}"

    fun isPanCardValid(panNo: String): Boolean {
        return Pattern.compile(PAN_REGEX).matcher(panNo).matches()
    }

    //https://www.quora.com/What-is-the-format-of-a-driver%E2%80%99s-License-number-in-India
    private const val DL_REGEX = "[A-Za-z]{2}[0-9]{2}[A-Z0-9a-z]{3}[0-9]{8}"
    fun isDLNumberValid(dlNo: String): Boolean {
        val sanitizedDLNo = dlNo.removeAll("-", " ", "/")
        return Pattern.compile(DL_REGEX).matcher(sanitizedDLNo).matches()
    }

    //https://en.wikipedia.org/wiki/Indian_Financial_System_Code
    private const val IFSC_REGEX = "[A-Za-z]{4}0[A-Z0-9a-z]{6}"
    fun isIfSCValid(ifsc: String): Boolean {
        return Pattern.compile(IFSC_REGEX).matcher(ifsc).matches()
    }

    private fun String.removeAll(vararg c: String): String {
        var resultString = this

        c.forEach {
            resultString = resultString.replace(it, "")
        }

        return resultString
    }

}