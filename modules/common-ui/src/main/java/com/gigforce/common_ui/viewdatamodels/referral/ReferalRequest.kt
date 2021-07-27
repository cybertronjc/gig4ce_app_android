package com.gigforce.common_ui.viewdatamodels.referral

import com.google.gson.annotations.SerializedName

data class ReferralRequest(

    @SerializedName("referralType")
    val referralType : String,

    @SerializedName("mobileNumber")
    val mobileNumber : String,

    @SerializedName("jobProfileName")
    val jobProfileName : String,

    @SerializedName("userName")
    val userName : String,

    @SerializedName("shareLink")
    val shareLink : String,
)


