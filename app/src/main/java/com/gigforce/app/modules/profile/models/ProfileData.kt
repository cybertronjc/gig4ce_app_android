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
    val dob: Date? = null,
    var rating: Float? = null,
    val tasksDone: Int = 0,
    val connections: Int = 0
) {

    fun GetAge(): String {
        return "-"
    }

    fun GetRating(): Float {
        return rating!!
    }

    fun SetRating(rting: Float) {
        rating = rting
    }

    fun GetTasksDone(): Int {
        return tasksDone
    }

    fun GetConnections(): Int {
        return connections
    }

}