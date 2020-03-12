package com.gigforce.app.modules.profile.models

import java.util.*

data class Experience(
    var title: String = "",
    var employmentType: String = "",
    var location: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    var currentExperience: Boolean = false
){
}