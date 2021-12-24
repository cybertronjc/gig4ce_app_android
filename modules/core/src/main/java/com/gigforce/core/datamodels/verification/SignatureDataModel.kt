package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignatureDataModel(

        @SerializedName("fullSignatureUrl")
        var fullSignatureUrl: String? = "",

        @SerializedName("signaturePathOnFirebase")
        var signaturePathOnFirebase: String? = "",

        @SerializedName("backGroundRemoved")
        var backGroundRemoved : Boolean = false

) : VerificationUserSubmittedData, Parcelable
