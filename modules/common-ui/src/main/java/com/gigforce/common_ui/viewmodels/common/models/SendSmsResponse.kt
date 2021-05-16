package com.gigforce.common_ui.viewmodels.common.models

import com.google.gson.annotations.SerializedName

data class SendSmsResponse (

    @SerializedName("error")
    val error: String? = null
    )