package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.google.gson.annotations.SerializedName

data class RegisterMobileNoRequest(
    @SerializedName("mobileNo")
    val mobileNo: String
)

data class RegisterMobileNoResponse(
    @SerializedName("verificationToken")
    val verificationToken: String? = null,

    @SerializedName("isUserAlreadyRegistered")
    val isUserAlreadyRegistered: Boolean = false
)

data class VerifyOtpResponse(
    @SerializedName("msg")
    val msg: String? = null,

    @SerializedName("isVerified")
    val isVerified: Boolean = true
)

data class CreateUserRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

data class CreateUserResponse(
    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("uId")
    val uid: String? = null,

    @SerializedName("error")
    val error: String? = null
)