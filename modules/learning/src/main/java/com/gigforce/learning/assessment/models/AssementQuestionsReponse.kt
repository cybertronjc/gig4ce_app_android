package com.gigforce.learning.assessment.models

data class AssementQuestionsReponse(
    var assessment: ArrayList<AssesmentArr>? = ArrayList(),
    var lesson_id: String = "",
    var scenario: String = "",
    var type: String = "",
    var duration: String = "00:05:00",
    var Name: String = "",
    var level: Int = 1,
    var assessment_image: String = "",
    var timeTakenInMillis: Long = 0,
    var passing_percentage: Int = 100
)