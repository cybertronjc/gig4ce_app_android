package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class AssignGigResponse(

    @SerializedName("success")
    var success: Boolean = false,

    @SerializedName("message")
    var message: String? = null
)
