package com.gigforce.app.modules.questionnaire.models

import com.gigforce.app.modules.questionnaire.AdapterOptionsQuestionnaire
import com.google.firebase.firestore.Exclude
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