package com.gigforce.app.modules.roster.models

data class Gig (
    var tag: String = "",
    var gigStatus: String = "upcoming",
    var startHour: Int = 0,
    var startMinute: Int = 0,
    var duration: Float = 0.0F
) {

}