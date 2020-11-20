package com.gigforce.app.modules.landingscreen.models

data class Responsibilities(
    var lessPointsNumber: Int = 0, @JvmField var showLessPoints: Boolean,
    var responsibilities: ArrayList<String>? = null,
    var title: String? = null
)