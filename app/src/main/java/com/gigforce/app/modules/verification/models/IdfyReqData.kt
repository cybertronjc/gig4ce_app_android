package com.gigforce.app.modules.verification.models

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class PostDataOCR(
        @SerializedName("task_id") var task_id: String,
        @SerializedName("group_id") var group_id: String,
        @SerializedName("data") var data: OCRDocData
)

data class OCRDocData(
        @SerializedName("document1") var document1: String,
        @SerializedName("consent") var consent: String
)