package com.gigforce.app.modules.assessment.models

data class AssementQuestionsReponse(
    var assessment: ArrayList<AssesmentArr>? = ArrayList(),
    var lesson_id: String = "",
    var scenario: String = "",
    var type: String = "",
    var duration: String = "00:04:00",
    var assessment_name: String = "Retail Sales Executive",
    var level: Int = 1,
    var assessment_image: String = "https://image.freepik.com/free-photo/woman-buying-grocery-store_1303-14084.jpg",
    var timeTakenInMillis: Long = 0,
    var passing_percentage: Int = 50
)