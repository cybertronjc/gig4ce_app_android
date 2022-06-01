package com.gigforce.app.domain.models.tl_workspace

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class TLWorkSpaceSectionApiModel(

    @field:SerializedName("index")
    val index: Int? = null,

    @field:SerializedName("filters")
    val filters: List<FiltersItemApiModel>? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("sectionId")
    val sectionId: String? = null,

    @field:SerializedName("items")
    val items: List<SectionItemApiModel>? = null,

    @field:SerializedName("upcomingGigers")
    val upcomingGigers : List<UpcomingGigersApiModel>? = null
)

data class FiltersItemApiModel(

    @field:SerializedName("endDate")
    val endDate: LocalDate? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("filterId")
    val filterId: String? = null,

    @field:SerializedName("startDate")
    val startDate: LocalDate? = null,

    @field:SerializedName("default")
    val default: Boolean? = false,

    @field:SerializedName("customDateOrRangeFilter")
    var customDateOrRangeFilter: Boolean,

    @field:SerializedName("customFilterSelectRange")
    var selectRangeInFilter: Boolean? = false,

    @field:SerializedName("maxDaysDifferenceInCaseOfRange")
    var maxDaysDifferenceInCaseOfRange: Int? = -1,

    @field:SerializedName("defaultSelectedDate")
    val defaultSelectedDate: LocalDate? = null,

    @field:SerializedName("minimumDateAvailableForSelection")
    val minimumDateAvailableForSelection: LocalDate? = null,

    @field:SerializedName("maximumDateAvailableForSelection")
    val maximumDateAvailableForSelection: LocalDate? = null,

    ) {

    fun mapToPresentationFilter(): TLWorkSpaceDateFilterOption {
        return TLWorkSpaceDateFilterOption(
            filterId = filterId
                ?: throw IllegalStateException("mapToPresentationFilter() - no filter id found in api model FiltersItem model"),
            text = text
                ?: throw IllegalStateException("mapToPresentationFilter() - no filter text found in api model FiltersItem model"),
            startDate = startDate,
            endDate = endDate,
            selected = false,
            default = default ?: false,
            customDateOrRangeFilter = customDateOrRangeFilter,
            selectRangeInFilter = selectRangeInFilter ?: false,
            maxDaysDifferenceInCaseOfRange = maxDaysDifferenceInCaseOfRange ?: -1,
            defaultSelectedDate = defaultSelectedDate,
            minimumDateAvailableForSelection = minimumDateAvailableForSelection,
            maximumDateAvailableForSelection = maximumDateAvailableForSelection,
        )
    }
}

data class SectionItemApiModel(

    @field:SerializedName("cardId")
    val cardId: String? = null,

    @field:SerializedName("valueChangeType")
    val valueChangeType: String? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("valueChangedBy")
    val valueChangedBy: Int? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("cardIndex")
    val cardIndex: Int? = null


)

data class UpcomingGigersApiModel(
    @field:SerializedName("gigerId")
    val gigerId: String? = null,

    @field:SerializedName("name")
    val gigerName: String? = null,

    @field:SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @field:SerializedName("business")
    val business: String? = null,

    @field:SerializedName("jobProfile")
    val jobProfile: String? = null,

    @field:SerializedName("profilePicture")
    val profilePicture: String? = null,

    @field:SerializedName("profilePictureThumbnail")
    val profilePictureThumbnail: String? = null,
){

    fun hasSameContentAs(
        data: UpcomingGigersApiModel
    ): Boolean {
        return this.gigerId == data.gigerId &&
                this.gigerName == data.gigerName &&
                this.jobProfile == data.jobProfile &&
                this.business == data.business &&
                this.profilePicture == data.profilePicture &&
                this.profilePictureThumbnail == data.profilePictureThumbnail
    }
}
