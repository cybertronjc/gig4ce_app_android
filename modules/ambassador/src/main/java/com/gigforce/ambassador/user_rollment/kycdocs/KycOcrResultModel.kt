package com.gigforce.ambassador.user_rollment.kycdocs

import com.google.gson.annotations.SerializedName

data class KycOcrResultModel(
    @SerializedName("status")
    val status: Boolean = false,
    @SerializedName("message")
    val message: String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = "",
    //pancard
    @SerializedName("panNumber")
    val panNumber: String? = "",
    @SerializedName("fatherName")
    val fatherName: String? = "",
    //aadhar
    @SerializedName("aadhaarNumber")
    val aadhaarNumber: String? = "",
    @SerializedName("gender")
    val gender: String? = "",
    //dl
    @SerializedName("dlNumber")
    val dlNumber: String? = "",
    @SerializedName("validTill")
    val validTill: String? = "",
    //bank
    @SerializedName("ifscCode")
    val ifscCode: String? = "",
    @SerializedName("beneficiaryName")
    val beneficiaryName: String? = "",
    @SerializedName("accountNumber")
    val accountNumber: String? = "",
    @SerializedName("bankName")
    val bankName: String? = ""

)