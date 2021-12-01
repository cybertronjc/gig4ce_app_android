package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AadhaarDetailsDataModel(

        @SerializedName("frontImagePath")
        var frontImagePath: String? = "",

        @SerializedName("backImagePath")
        var backImagePath: String? = "",

        @SerializedName("aadhaarCardNo")
        var aadhaarCardNo: String? = "",

        @SerializedName("dateOfBirth")
        var dateOfBirth: String = "",

        @SerializedName("fName")
        var fName: String = "",

        @SerializedName("addLine1")
        var addLine1: String = "",

        @SerializedName("addLine2")
        var addLine2: String = "",

        @SerializedName("state")
        var state: String = "",

        @SerializedName("city")
        var city: String = "",

        @SerializedName("pincode")
        var pincode: String? = "",

        @SerializedName("landmark")
        var landmark: String? = "",

        @SerializedName("currentAddSameAsParmanent")
        var currentAddSameAsParmanent: Boolean = true,

        @SerializedName("currentAddress")
        var currentAddress: CurrentAddressDetailDataModel? = null,

        @SerializedName("name")
        var name: String? = "",

        @SerializedName("verified")
        var verified : Boolean?=false
) : VerificationUserSubmittedData, Parcelable

@Parcelize
data class CurrentAddressDetailDataModel(

        @SerializedName("addLine1")
        var addLine1: String? = "",

        @SerializedName("addLine2")
        var addLine2: String? = "",

        @SerializedName("state")
        var state: String? = "",

        @SerializedName("city")
        var city: String? = "",

        @SerializedName("pincode")
        var pincode: String? = "",

        @SerializedName("landmark")
        var landmark: String? = ""
) : Parcelable