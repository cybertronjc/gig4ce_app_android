package com.gigforce.core.datamodels.auth

import com.google.gson.annotations.SerializedName

data class UserAuthStatusModel(

        @field:SerializedName("status")
        val status: Boolean = false,

        @field:SerializedName("isUserRegistered")
        val isUserRegistered : Boolean = false,

        @field:SerializedName("uId")
        val uId : String = "")
{
}