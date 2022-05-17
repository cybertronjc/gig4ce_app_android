package com.gigforce.app.data.remote.models.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.google.gson.annotations.SerializedName

data class GetUpcomingGigersResponse(

    @field:SerializedName("upcomingGigers")
    val upcomingGigers: List<UpcomingGigersApiModel>
)
