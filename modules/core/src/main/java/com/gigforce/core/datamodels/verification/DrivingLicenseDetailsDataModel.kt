package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrivingLicenseDetailsDataModel(

    @SerializedName("name")
    var name: String? = "",

    @SerializedName("drivingLicenseNo")
    var drivingLicenseNo: String? = "",

    @SerializedName("issueDate")
    var issueDate: String? = "",

    @SerializedName("expiryDate")
    var expiryDate: String? = "",

    @SerializedName("dateOfBirth")
    var dateOfBirth: String? = ""

) : VerificationUserSubmittedData, Parcelable
