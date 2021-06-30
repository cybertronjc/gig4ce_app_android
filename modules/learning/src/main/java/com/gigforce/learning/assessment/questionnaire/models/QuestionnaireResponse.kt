package com.gigforce.learning.assessment.questionnaire.models

data class QuestionnaireResponse(
    var type: String = "",
    var questions: List<Questions> = listOf(),
    var jobProfileId: String = "",
    var title: String = "",
    var rejectionTitle: String = "",
    var rejectionIllustration: String = ""
)