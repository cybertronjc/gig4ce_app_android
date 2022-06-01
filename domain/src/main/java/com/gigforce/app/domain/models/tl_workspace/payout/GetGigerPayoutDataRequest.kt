package com.gigforce.app.domain.models.tl_workspace.payout

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.google.gson.annotations.SerializedName

data class GetGigerPayoutDataRequest(
    @field:SerializedName("filter")
    val filter: FiltersItemApiModel? = null
) {
}