package com.gigforce.app.modules.common.models

import com.google.gson.annotations.SerializedName

data class SendSmsRequest(

        @SerializedName("phoneNumber")
        val phoneNumber: String,

        @SerializedName("message")
        val message: String,
)