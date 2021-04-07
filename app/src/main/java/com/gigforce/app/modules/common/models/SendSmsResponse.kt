package com.gigforce.app.modules.common.models

import com.google.gson.annotations.SerializedName

data class SendSmsResponse (

    @SerializedName("error")
    val error: String? = null
    )