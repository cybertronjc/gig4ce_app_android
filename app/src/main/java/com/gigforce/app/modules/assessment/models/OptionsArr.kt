package com.gigforce.app.modules.assessment.models

data class OptionsArr(

    var que: String = "",
    var reason: String = "",
    var selectedAnswer: Boolean? = false,
    var clickStatus: Boolean? = true,
    @field:JvmField var is_answer: Boolean? = true
)