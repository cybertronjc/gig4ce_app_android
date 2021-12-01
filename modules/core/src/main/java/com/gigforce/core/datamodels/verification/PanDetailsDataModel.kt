package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PanDetailsDataModel(

        @SerializedName("panCardImagePath")
        var panCardImagePath: String? = "",

        @SerializedName("panCardNo")
        var panCardNo: String? = ""

) : VerificationUserSubmittedData, Parcelable
