package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class JobProfileOverview(

    @SerializedName("jobProfileId")
    val jobProfileId: String,

    @SerializedName("tradeName")
    val tradeName: String?,

    @SerializedName("profileName")
    val profileName: String?,

    @SerializedName("companyLogo")
    val companyLogo: String?,

    @SerializedName("ongoing")
    val ongoing: Boolean,

    @SerializedName("submitted")
    val submitted: Boolean,

    @SerializedName("status")
    val status: String?,

    var isSelected :Boolean = false
)
