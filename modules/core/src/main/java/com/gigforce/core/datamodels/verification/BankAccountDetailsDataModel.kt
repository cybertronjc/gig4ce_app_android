package com.gigforce.core.datamodels.verification

import com.google.gson.annotations.SerializedName


data class BankAccountDetailsDataModel(

        @SerializedName("bankName")
        var bankName: String? = "",

        @SerializedName("ifsc")
        var ifsc: String? = "",

        @SerializedName("bankAccountNumber")
        var bankAccountNumber: String? = ""

) : VerificationUserSubmittedData

