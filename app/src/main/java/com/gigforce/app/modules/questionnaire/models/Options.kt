package com.gigforce.app.modules.questionnaire.models

data class Options(
    @field:JvmField var isAnswer: Boolean = false,
    var question: String = "",
    var type: String = "",
    var options: MutableList<String> = mutableListOf()
)