package com.gigforce.core

import java.util.regex.Pattern

object ValidationHelper {

    private val INDIAN_MOBILE_NUMBER = Pattern.compile("^[6-9][0-9]{9}\$")

    fun isValidIndianMobileNo(
        tenDigitMobileNo : String
    ) : Boolean{
        return INDIAN_MOBILE_NUMBER.matcher(tenDigitMobileNo).matches()
    }

}