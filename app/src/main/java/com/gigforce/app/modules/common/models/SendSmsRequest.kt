package com.gigforce.app.modules.common.models

import com.google.gson.annotations.SerializedName

data class SendSmsRequest(

        @SerializedName("phoneNumber")
        val phoneNumber: String,

        @SerializedName("message")
        val message: String,

        @SerializedName("type")
        val type: String,

        @SerializedName("userName")
        val userName: String? = null,

        @SerializedName("enrollAmbassadorShareLink")
        val enrollAmbassadorShareLink: String? = null,
)