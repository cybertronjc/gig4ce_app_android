package com.gigforce.app.modules.gigerVerfication

import java.util.regex.Pattern

object VerificationValidations {

    //https://en.wikipedia.org/wiki/Permanent_account_number
    private const val PAN_REGEX = "[A-Z]{5}[0-9]{4}[A-Z]{1}"

    fun isPanCardValid(panNo : String) : Boolean{
      return Pattern.compile(PAN_REGEX).matcher(panNo).matches()
    }

    //https://www.quora.com/What-is-the-format-of-a-driver%E2%80%99s-License-number-in-India
    private const val DL_REGEX = "[A-Z]{2}[0-9]{13}"
    fun isDLNumberValid(panNo : String) : Boolean{
        return Pattern.compile(DL_REGEX).matcher(panNo).matches()
    }

}