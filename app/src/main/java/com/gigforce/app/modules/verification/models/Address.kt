package com.gigforce.app.modules.verification.models

import java.util.*

data class Address(
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var pincode: String = ""
){
}