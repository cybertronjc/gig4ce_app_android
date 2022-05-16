package com.gigforce.app.domain.models.tl_workspace

import java.time.LocalDate
import java.time.LocalDateTime

data class TLWorkSpaceFilterOption(
    val filterId: String,
    val text: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    var selected: Boolean,
    var default: Boolean,
    var customDateOrRangeFilter: Boolean,
    var selectRangeInFilter: Boolean,
    var maxDaysDifferenceInCaseOfRange: Int = 7
) {

    fun mapToApiModel(): FiltersItemApiModel {
        return FiltersItemApiModel(
            endDate = endDate,
            text = text,
            filterId = filterId,
            startDate = startDate,
            default = default,
            customDateOrRangeFilter = customDateOrRangeFilter,
            selectRangeInFilter = selectRangeInFilter,
            maxDaysDifferenceInCaseOfRange = maxDaysDifferenceInCaseOfRange
        )
    }

}