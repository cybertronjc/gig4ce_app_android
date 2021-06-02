package com.gigforce.client_activation.client_activation.models

data class Requirements(
    var requirements: ArrayList<String>? = null,
    @JvmField var showLessPoints: Boolean = false,
    var lessPointsNumber: Int = 0,
    var title: String? = null
)