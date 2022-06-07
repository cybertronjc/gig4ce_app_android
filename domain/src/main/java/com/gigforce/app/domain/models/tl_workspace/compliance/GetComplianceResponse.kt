package com.gigforce.app.domain.models.tl_workspace.compliance

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.gigforce.app.domain.models.tl_workspace.retention.StatusMasterWithCountItem
import com.google.gson.annotations.SerializedName

data class GetComplianceResponse(

    @field:SerializedName("pendingTypeMaster")
    val pendingTypeMaster: List<StatusMasterWithCountItem>? = null,

    @field:SerializedName("gigersWithComplainceData")
    val gigersWithPendingComplainceData: List<GigersWithPendingComplainceDataItem>? = null,

    @field:SerializedName("filters")
    val filters: List<FiltersItemApiModel>? = null
)

data class PendingTypeMasterItem(

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null
)

data class GigersWithPendingComplainceDataItem(

    @field:SerializedName("jobProfile")
    val jobProfile: String? = null,

    @field:SerializedName("jobProfileId")
    val jobProfileId: String? = null,

    @field:SerializedName("business")
    val business: String? = null,

    @field:SerializedName("gigerId")
    val gigerId: String? = null,

    @field:SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("compliancePending")
    val compliancePending: List<String>? = null,

    @field:SerializedName("warningText")
    val warningText: String? = null,

    @field:SerializedName("profilePicThumbnail")
    val profilePicThumbnail: String? = null,

    @field:SerializedName("profileAvatarName")
    val profileAvatarName: String? = null,
) {

    fun getBusinessNonNull(): String {
        return business ?: "Others"
    }

}