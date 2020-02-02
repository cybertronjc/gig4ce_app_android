package com.gigforce.app.modules.profile.models

import java.util.*

data class Education(
    var institution: String = "",
    var course: String = "",
    var degree: String = "",
    var startYear: Date? = null,
    var endYear: Date? = null
){
}