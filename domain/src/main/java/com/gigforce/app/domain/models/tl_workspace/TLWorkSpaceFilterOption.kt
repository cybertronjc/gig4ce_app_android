package com.gigforce.app.domain.models.tl_workspace

import java.time.LocalDate
import java.time.LocalDateTime

data class TLWorkSpaceFilterOption(
    val filterId : String,
    val text : String,
    val startDate : LocalDateTime?,
    val endDate : LocalDateTime?,
    var selected : Boolean
)