package com.gigforce.app.domain.models.tl_workspace.compliance

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.google.gson.annotations.SerializedName


data class GetCompliancePendingRequest(

    @field:SerializedName("filter")
    val filter: FiltersItemApiModel? = null
)