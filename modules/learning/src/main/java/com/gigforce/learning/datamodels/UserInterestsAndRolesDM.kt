package com.gigforce.learning.datamodels

import com.google.firebase.firestore.PropertyName
import java.util.*
import kotlin.collections.ArrayList

data class UserInterestsAndRolesDM (
    var status: Boolean = true,
    var errormsg: String = "",
    var id: String? = null,
    var role_interests: ArrayList<RoleInterests>? = null,
    @get:PropertyName("companies")
    @set:PropertyName("companies")
    var companies: ArrayList<Company>? = null,
)

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

data class Company(

    @get:PropertyName("companyId")
    @set:PropertyName("companyId")
    var companyId: String = "",

    @get:PropertyName("companyName")
    @set:PropertyName("companyName")
    var companyName: String? = ""
)