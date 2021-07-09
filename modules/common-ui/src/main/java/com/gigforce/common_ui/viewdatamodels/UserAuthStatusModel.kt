package com.gigforce.common_ui.viewdatamodels

import com.google.gson.annotations.SerializedName

data class UserAuthStatusModel(

        @field:SerializedName("status")
        val status: Boolean = false,

        @field:SerializedName("isUserRegistered")
        val isUserRegistered : Boolean = false) {
}