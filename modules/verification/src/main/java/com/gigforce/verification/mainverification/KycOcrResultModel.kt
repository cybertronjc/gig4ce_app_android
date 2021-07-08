package com.gigforce.verification.mainverification

import com.google.gson.annotations.SerializedName

data class KycOcrResultModel(

    @field:SerializedName("status")
    val status: Boolean = false,

    @field:SerializedName("message")
    val message: String? = "") {
}