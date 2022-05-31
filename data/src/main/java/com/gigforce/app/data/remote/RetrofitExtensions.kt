package com.gigforce.app.data.remote

import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.retrofit.GsonExclusionStrategy
import com.google.gson.GsonBuilder
import retrofit2.HttpException
import retrofit2.Response

fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body()!!
}


inline fun <reified T> Response<BaseResponse<T>>.bodyFromBaseResponseElseThrow(): T {

    return when (code()) {
        in 200..299 -> body()!!.getBodyOrThrow()
        in 400..499 -> throw Exception(getErrorMessageFromErrorBody())
        else -> throw HttpException(this)
    }
}


inline fun <reified T> Response<BaseResponse<T>>.getErrorMessageFromErrorBody(): String {
    val errorBodyString = errorBody()?.string() ?: throw HttpException(this)

    val gson = GsonBuilder()
        .setExclusionStrategies(GsonExclusionStrategy())
        .setLenient()
        .serializeNulls()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    try {
        return gson.fromJson(
            errorBodyString,
            BaseResponse::class.java
        ).message!!
    } catch (e: Exception) {
        val logger = GigforceLogger()
        logger.e(
            tag = "tryConvertingErrorBodyToJson()",
            occurredWhen = buildString {
                append("Unable to convert error body to pojo")
                append("\n")
                append("Error body : ")
                append(errorBodyString)
                append("\n")
                append("Converting to Class name :")
                append(T::class.simpleName)
            },
            e = e
        )

        return "Unable to process server response, please check again later"
    }
}