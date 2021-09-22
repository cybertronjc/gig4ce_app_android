package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class JoiningBusiness(
    @SerializedName("id")
    var id: String = "",

    @SerializedName("name")
    var name: String = ""
)
