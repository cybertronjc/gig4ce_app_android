package com.gigforce.app.modules.assessment.models

import com.google.gson.annotations.SerializedName

data class AssementQuestionsReponse(
        var assessment: ArrayList<AssesmentArr>? = ArrayList(),
        var lesson_id: String = "",
        var scenario: String = "",
        var type: String = "",
        var duration: String = "00:15:00",
        @SerializedName("name") var assessment_name: String = "",
        var level: Int = 1,
        var assessment_image: String = "",
        var timeTakenInMillis: Long = 0,
        var passing_percentage: Int = 50
)