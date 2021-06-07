package com.gigforce.learning.assessment.questionnaire.models

data class OpenDates(
    @JvmField var openAllDates: Boolean = false,
    @JvmField var openFutureDates: Boolean = false,
    @JvmField var openPastDates: Boolean = false
)