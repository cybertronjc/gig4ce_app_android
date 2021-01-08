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
        var selectedCity: String = "",
        var type: String = "",
        var rejectionPoint: String = "",
        var openDates: OpenDates? = null,
        var validation: DateValidation? = null,
        var answer: String = ""


)