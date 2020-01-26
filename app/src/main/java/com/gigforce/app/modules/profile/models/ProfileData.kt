package com.gigforce.app.modules.profile.models

import java.util.*
import kotlin.collections.ArrayList

data class ProfileData(
    val id: String?,
    val name: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val max_education: String? = null,
    val current_edu: String? = null,
    val current_work: String? = null,
    val tags: ArrayList<String> = ArrayList(),
    val dob: Date? = null
) {

    fun GetAge(): String {
        return "-"
    }

}