package com.gigforce.app.modules.profile.models

import java.util.*

data class Experience(
    var company: String = "",
    var position: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null
){
}