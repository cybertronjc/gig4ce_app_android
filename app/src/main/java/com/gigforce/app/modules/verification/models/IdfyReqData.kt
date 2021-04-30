package com.gigforce.app.modules.verification.models

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class PostDataOCR(
        @SerializedName("task_id") var task_id: String,
        @SerializedName("group_id") var group_id: String,
        @SerializedName("data") var data: OCRDocData
)

data class PostDataOCRs(
        @SerializedName("task_id") var task_id: String,
        @SerializedName("group_id") var group_id: String,
        @SerializedName("data") var data: OCRDocsData
)

data class OCRDocData(
        @SerializedName("document1") var document1: String,
        @SerializedName("consent") var consent: String
)

data class OCRDocsData(
        @SerializedName("document1") var document1: String,
        @SerializedName("document2") var document2: String,
        @SerializedName("consent") var consent: String
)


data class PostDataDL(
        @SerializedName("task_id") var task_id: String,
        @SerializedName("group_id") var group_id: String,
        @SerializedName("data") var data: DLDocData
)

data class DLDocData(
        @SerializedName("document1") var document1: String,
        @SerializedName("consent") var consent: String
)

data class PostDataPAN(
        @SerializedName("task_id") var task_id: String,
        @SerializedName("group_id") var group_id: String,
        @SerializedName("data") var data: PANDocData
)

data class PANDocData(
        @SerializedName("document1") var document1: String,
        @SerializedName("document2") var document2: String,
        @SerializedName("consent") var consent: String
)