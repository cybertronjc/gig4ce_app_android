package com.gigforce.learning.assessment.questionnaire.models

data class DateValidation(
    @JvmField val inRangeRequire: Boolean = false,
    @JvmField val outRangeRequire: Boolean = false,
    @JvmField var validationRequire: Boolean = false,
    var inRange: InRange? = null,
    var outRange: OutRange? = null

)