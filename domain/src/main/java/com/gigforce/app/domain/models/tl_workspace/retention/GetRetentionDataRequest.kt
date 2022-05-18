package com.gigforce.app.domain.models.tl_workspace.retention

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.google.gson.annotations.SerializedName


data class GetRetentionDataRequest(

    @field:SerializedName("filter")
    val filter: FiltersItemApiModel? = null
)