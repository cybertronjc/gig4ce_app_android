package com.gigforce.app.modules.profile.models

import java.util.*

data class Achievement(
    var title: String = "",
    var issuingAuthority: String = "",
    var location: String = "",
    var year: Date? = null
){
}