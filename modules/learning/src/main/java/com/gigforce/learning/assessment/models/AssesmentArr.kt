package com.gigforce.learning.assessment.models

data class AssesmentArr(
    var options: ArrayList<OptionsArr>? = ArrayList<OptionsArr>(),
    var question: String = "",
    var answered: Boolean = false
) {
}