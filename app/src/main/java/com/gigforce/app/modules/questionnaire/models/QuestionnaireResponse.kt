package com.gigforce.app.modules.questionnaire.models

data class QuestionnaireResponse(
    var type: String = "",
    var questions: List<Questions> = listOf(),
    var jobProfileId: String = "",
    var title: String = "",
    var rejectionTitle: String = ""
)