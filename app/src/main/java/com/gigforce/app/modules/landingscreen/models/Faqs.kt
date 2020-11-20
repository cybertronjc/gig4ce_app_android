package com.gigforce.app.modules.landingscreen.models

data class Faqs(
    var title: String? = null,
    var lessPointsNumber: Int = 0,
    @JvmField var showLessPoints: Boolean = false,
    var questions: ArrayList<String>? = null
)
