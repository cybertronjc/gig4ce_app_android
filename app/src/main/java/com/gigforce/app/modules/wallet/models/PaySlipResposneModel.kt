package com.gigforce.app.modules.wallet.models

import com.google.gson.annotations.SerializedName

data class PaySlipResponseModel(

    @SerializedName("message")
    var message : String,

    @SerializedName("downloadLink")
    var downloadLink : String?
)