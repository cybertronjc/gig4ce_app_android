package com.gigforce.app.core.validation

import java.util.regex.Pattern

// starter code for validation
object Regexes {

    // Login auth
    val INDIAN_MOBILE_NUMBER = Pattern.compile("^[+][0-9]{12}\$")
    val PERSON_NAME = Pattern.compile("^[A-Za-z ]+")


    // Verification
    val ADDRESS =
        Pattern.compile("^(\\w+\\s*[\\#\\-\\,\\/\\.\\(\\)\\&]*)+")!!
    val CITY_STATE =
        Pattern.compile("^(\\w+\\s*\\w*)+")
    val PINCODE =
        Pattern.compile("^([0-9]{6}|[0-9]{3}\\s*[0-9]{3})")

    // Profile


    // Preference


    // settings


    //

}