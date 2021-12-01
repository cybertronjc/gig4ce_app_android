package com.gigforce.core.datamodels.verification

import com.google.gson.annotations.SerializedName


data class PanDetailsDataModel(

        @SerializedName("panCardImagePath")
        var panCardImagePath: String? = "",

        @SerializedName("panCardNo")
        var panCardNo: String? = ""

) : VerificationUserSubmittedData
