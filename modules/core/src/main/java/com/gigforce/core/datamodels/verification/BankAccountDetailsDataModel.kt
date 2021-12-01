package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BankAccountDetailsDataModel(

        @SerializedName("bankName")
        var bankName: String? = "",

        @SerializedName("ifsc")
        var ifsc: String? = "",

        @SerializedName("bankAccountNumber")
        var bankAccountNumber: String? = ""

) : VerificationUserSubmittedData, Parcelable

