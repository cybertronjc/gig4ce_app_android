package com.gigforce.app.domain.models.tl_workspace

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class TLWorkSpaceSectionApiModel(

    @field:SerializedName("index")
    val index: Int? = null,

    @field:SerializedName("filters")
    val filters: List<FiltersItemApiModel>? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("items")
    val items: List<SectionItemApiModel>? = null,

    @field:SerializedName("upcomingGigers")
    val upcomingGigers : List<UpcomingGigersApiModel>? = null
)

data class FiltersItemApiModel(

    @field:SerializedName("endDate")
    val endDate: LocalDateTime? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("filterId")
    val filterId: String? = null,

    @field:SerializedName("startDate")
    val startDate: LocalDateTime? = null,

    @field:SerializedName("default")
    val default: Boolean? = false,
) {

    fun mapToPresentationFilter(): TLWorkSpaceFilterOption {
        return TLWorkSpaceFilterOption(
            filterId = filterId
                ?: throw IllegalStateException("mapToPresentationFilter() - no filter id found in api model FiltersItem model"),
            text = text
                ?: throw IllegalStateException("mapToPresentationFilter() - no filter text found in api model FiltersItem model"),
            startDate = startDate,
            endDate = endDate,
            selected = false,
            default = default ?: false,

        )
    }
}

data class SectionItemApiModel(

    @field:SerializedName("valueChangeType")
    val valueChangeType: String? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("valueChangedBy")
    val valueChangedBy: Int? = null,

    @field:SerializedName("title")
    val title: String? = null
)

data class UpcomingGigersApiModel(
    @field:SerializedName("gigerId")
    val gigerId: String? = null,

    @field:SerializedName("gigerName")
    val gigerName: String? = null,

    @field:SerializedName("business")
    val business: String? = null,

    @field:SerializedName("jobProfile")
    val jobProfile: String? = null,

    @field:SerializedName("profilePicture")
    val profilePicture: String? = null,

    @field:SerializedName("profilePictureThumbnail")
    val profilePictureThumbnail: String? = null,
)
