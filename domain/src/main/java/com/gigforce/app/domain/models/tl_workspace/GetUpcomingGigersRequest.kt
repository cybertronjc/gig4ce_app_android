package com.gigforce.app.domain.models.tl_workspace

import com.google.gson.annotations.SerializedName


data class GetUpcomingGigersRequest(


    @field:SerializedName("filter")
    val filter: FiltersItemApiModel? = null
)