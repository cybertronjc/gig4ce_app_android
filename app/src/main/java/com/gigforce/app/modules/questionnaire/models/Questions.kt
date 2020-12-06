package com.gigforce.app.modules.questionnaire.models

import java.util.*

data class Questions(
        var question: String = "",
        var url: String = "",
        var options: List<Options> = listOf(),
        var dropDownItem: String = "",
        var selectedAnswer: Int = -1,
        var selectedDate: Date? = null,
        var selectedState: String = "",
        var selectedCity: String = ""

)