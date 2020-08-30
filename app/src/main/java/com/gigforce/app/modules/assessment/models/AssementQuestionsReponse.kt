package com.gigforce.app.modules.assessment.models

data class AssementQuestionsReponse(
    var assessment: ArrayList<AssesmentArr>? = ArrayList<AssesmentArr>(),
    var lesson_id: String="",
    var scenario: String="",
    var type: String=""
)