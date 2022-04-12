package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CharacterCertificateDataModel(

    @SerializedName("name")
    var name: String? = "",

    @SerializedName("path")
    var path: String? = "",

) : VerificationUserSubmittedData, Parcelable