package com.gigforce.app.modules.profile.models

import kotlin.collections.ArrayList

data class ProfileData(
    val id: String? = null,
    var name: String = "",
    var aboutMe: String = "",
    var email: String = "",
    var bio: String = "",
    var isVerified: Boolean = false,
    var Education: ArrayList<Education>? = null,
    var Skill: ArrayList<Skill>? = null,
    var Achievement: ArrayList<Achievement>? = null,
    var Language: ArrayList<Language>? = null,
    var Contact: ArrayList<Contact>? = null,
    var Tags: ArrayList<String>? = null,
    var connections: Int = 0,
    var rating: Float = 0.0F,
    var tasksDone: Int = 0
) {

}