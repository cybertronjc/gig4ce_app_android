package com.gigforce.common_ui.viewdatamodels

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(

    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("data")
    val data: List<T>,
)