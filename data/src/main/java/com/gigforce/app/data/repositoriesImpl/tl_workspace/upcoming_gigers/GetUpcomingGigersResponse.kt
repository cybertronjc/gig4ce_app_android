package com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.google.gson.annotations.SerializedName

data class GetUpcomingGigersResponse(

    @field:SerializedName("upcomingGigers")
    val upcomingGigers: List<UpcomingGigersApiModel>
)
