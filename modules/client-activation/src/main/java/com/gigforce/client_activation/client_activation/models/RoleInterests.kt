package com.gigforce.client_activation.client_activation.models

import java.util.*

data class RoleInterests(
    var interestID: String? = "",
    var timeStamp: Date? = Date(),
    var lat: String = "",
    var lon: String = "",
    var invitedBy: String = ""
) {
    override fun equals(other: Any?): Boolean {
        val obj = other as RoleInterests
        return obj.interestID.equals(interestID)
    }
}