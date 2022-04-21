package com.gigforce.verification.mainverification

import com.google.gson.annotations.SerializedName

class KYCImageRecogModel(
    @field:SerializedName("type")
    val type: String = "",

    @field:SerializedName("uid")
    val uid : String = "")