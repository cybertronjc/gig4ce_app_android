package com.gigforce.core.retrofit

import com.google.gson.annotations.SerializedName

data class PaySlipResponseModel(

    @SerializedName("message")
    var message : String? = null,

    @SerializedName("downloadLink")
    var downloadLink : String?= null
)