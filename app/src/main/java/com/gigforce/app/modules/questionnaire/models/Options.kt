package com.gigforce.app.modules.questionnaire.models

data class Options(
        @field:JvmField var isAnswer: Boolean = false,
        var answer: String = "",
        var type: String = "",
        var options: MutableList<String> = mutableListOf(),
        var selectedItemPosition: Int = -1

)