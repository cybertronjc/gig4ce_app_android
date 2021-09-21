package com.gigforce.learning.assessment.models

data class OptionsArr(

    var que: String = "",
    var reason: String = "",
    var selectedAnswer: Boolean? = false,
    var clickStatus: Boolean? = true,
    var showReason: Boolean? = true,
    @field:JvmField var is_answer: Boolean? = true
)