package com.gigforce.verification.mainverification

import com.google.gson.annotations.SerializedName

data class KycOcrResultModel(
    @field:SerializedName("status")
    val status: Boolean = false,
    @field:SerializedName("message")
    val message: String? = "",
    @field:SerializedName("name")
    val name: String? = "",
    @field:SerializedName("dateOfBirth")
    val dateOfBirth: String? = "",
    //pancard
    @field:SerializedName("panNumber")
    val panNumber: String? = "",
    @field:SerializedName("fatherName")
    val fatherName: String? = "",
    //aadhar
    @field:SerializedName("aadhaarNumber")
    val aadhaarNumber: String? = "",
    @field:SerializedName("gender")
    val gender: String? = "",
    //dl
    @field:SerializedName("dlNumber")
    val dlNumber: String? = "",
    @field:SerializedName("validTill")
    val validTill: String? = "",
    //bank
    @field:SerializedName("ifscCode")
    val ifscCode: String? = "",
    @field:SerializedName("beneficiaryName")
    val beneficiaryName: String? = "",
    @field:SerializedName("accountNumber")
    val accountNumber: String? = "",
    @field:SerializedName("bankName")
    val bankName: String? = ""

)