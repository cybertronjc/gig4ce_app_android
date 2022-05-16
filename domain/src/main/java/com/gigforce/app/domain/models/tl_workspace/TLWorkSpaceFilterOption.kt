package com.gigforce.app.domain.models.tl_workspace

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

@Parcelize
data class TLWorkSpaceFilterOption(
    val filterId: String,
    val text: String,
    var startDate: LocalDate?,
    var endDate: LocalDate?,
    var selected: Boolean,
    var default: Boolean,

    /*
    Custom Filter Options
    */
    var customDateOrRangeFilter: Boolean,
    var selectRangeInFilter: Boolean,
    var defaultSelectedDate: LocalDate?,
    var maxDaysDifferenceInCaseOfRange: Int = 7,
    var minimumDateAvailableForSelection: LocalDate? = null,
    var maximumDateAvailableForSelection: LocalDate? = null,
) : Parcelable{

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