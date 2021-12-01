package com.gigforce.core.datamodels.verification

import com.google.gson.annotations.SerializedName

data class VerificationDocuments(

    @SerializedName("aadhaarDocument")
    var aadhaarDocument : AadhaarDetailsDataModel? = null,

    @SerializedName("bankAccountDetails")
    var bankAccountDetails : BankAccountDetailsDataModel? = null,

    @SerializedName("drivingLicenseDetails")
    var drivingLicenseDetails : DrivingLicenseDetailsDataModel? = null,

    @SerializedName("panDetails")
    var panDetails : PanDetailsDataModel? = null
)
