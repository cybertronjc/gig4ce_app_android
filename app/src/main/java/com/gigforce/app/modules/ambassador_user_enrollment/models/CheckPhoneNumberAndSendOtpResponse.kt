package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.google.gson.annotations.SerializedName

data class CheckPhoneNumberAndSendOtpResponse(

    @SerializedName("isUserAlreadyRegistered")
    var isUserAlreadyRegistered : Boolean = false,

    @SerializedName("otpSent")
    var otpSent : String? = null
)

data class CreateUserRequest(
    @SerializedName("phoneNumber")
    val phoneNumber : String
)

data class CreateUserResponse(
    @SerializedName("phoneNumber")
    val phoneNumber : String,

    @SerializedName("uId")
    val uid : String
)