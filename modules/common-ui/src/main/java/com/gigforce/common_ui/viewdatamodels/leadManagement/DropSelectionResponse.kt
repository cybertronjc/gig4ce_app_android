package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

class DropSelectionResponse(

    @SerializedName("status")
    var status: Boolean = false,

    @SerializedName("message")
    var message: String? = null
) {
}