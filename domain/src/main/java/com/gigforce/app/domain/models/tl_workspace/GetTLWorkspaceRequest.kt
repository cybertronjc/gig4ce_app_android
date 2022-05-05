package com.gigforce.app.domain.models.tl_workspace

import com.google.gson.annotations.SerializedName

data class GetTLWorkspaceRequest(

    @field:SerializedName("defaultRequest")
    val defaultRequest: Boolean = true,

    @field:SerializedName("requestedData")
    val requestedData: List<RequestedDataItem>? = null
) {

    companion object {

        fun defaultRequest(): GetTLWorkspaceRequest {
            return GetTLWorkspaceRequest()
        }
    }
}


data class RequestedDataItem(

    @field:SerializedName("filter")
    val filter: FiltersItemApiModel? = null,

    @field:SerializedName("type")
    val sectionId: String? = null
)
