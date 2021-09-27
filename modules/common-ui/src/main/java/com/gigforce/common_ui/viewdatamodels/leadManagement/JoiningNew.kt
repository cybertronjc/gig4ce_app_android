package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class JoiningNew(
    @SerializedName("_id")
    var _id: String,

    @SerializedName("assignGigsFrom")
    var assignGigsFrom: String,

    @SerializedName("gigerName")
    var gigerName: String,

    @SerializedName("gigerMobileNo")
    var gigerMobileNo: String,

    @SerializedName("gigerId")
    var gigerId: String,

    @SerializedName("business")
    var business: JoiningBusiness? = null,

    @SerializedName("profilePicture")
    var profilePicture: String? = null,

    @SerializedName("status")
    var status: String,

    @SerializedName("createdAt")
    var createdAt: String? = null,

    @SerializedName("updatedAt")
    var updatedAt: String? = null
)


