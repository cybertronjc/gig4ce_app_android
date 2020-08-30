package com.gigforce.app.modules.assessment.models

data class Assessment(
    val id: String,
    val status: Int,
    val title: String,
    val assessmentLength: String
) {

    companion object {

        const val STATUS_PENDING = 0
        const val STATU_COMPLETED = 1
    }
}
