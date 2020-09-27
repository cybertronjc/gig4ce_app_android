package com.gigforce.app.modules.profile.models

import java.util.*

data class RoleInterests(var interestID: String? = "", var timeStamp: Date? = Date()) {
    override fun equals(other: Any?): Boolean {
        val obj = other as RoleInterests
        return obj.interestID.equals(interestID)
    }
}