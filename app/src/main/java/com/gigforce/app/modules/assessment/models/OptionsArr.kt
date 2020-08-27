package com.gigforce.app.modules.assessment.models

data class OptionsArr(

    var que: String = "",
    var reason: String = "",
    var selectedAnswer: Boolean? = null,
    @field:JvmField var is_answer: Boolean? = true
)