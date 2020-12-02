package com.gigforce.app.modules.questionnaire.models

data class Questions(
    var question: String = "",
    var url: String = "",
    var options: List<Options> = listOf(),
    var dropDownItem: String = "",
    var selectedAnswer: Int = -1
)