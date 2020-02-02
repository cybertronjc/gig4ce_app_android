package com.gigforce.app.modules.profile.models

import kotlin.collections.ArrayList

data class ProfileData(
    val id: String? = null,
    var name: String = "",
    var email: String = "",
    var Education: ArrayList<Education>? = null,
    var connections: Int = 0,
    var rating: Float = 0.0F,
    var tasksDone: Int = 0
) {

}