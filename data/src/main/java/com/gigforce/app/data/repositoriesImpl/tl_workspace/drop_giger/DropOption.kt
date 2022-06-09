package com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger

import com.google.gson.annotations.SerializedName

data class DropOptionApiModel(

    @SerializedName("localizedReason")
    var dropLocalizedText: String,

    @SerializedName("reasonId")
    var reasonId: String,

    @SerializedName("customReason")
    var customReason: Boolean
)
