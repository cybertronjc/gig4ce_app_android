package com.gigforce.app.data.remote.models

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String?,

    @field:SerializedName("data")
    val data: T?
) {

    fun getBodyOrThrow(): T {
        if (data != null) {
            return data
        } else if (message != null) {
            throw Exception(message)
        } else {
            throw Exception("getBodyOrThrow() data and message both are null")
        }
    }
}
