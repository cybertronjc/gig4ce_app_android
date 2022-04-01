package com.gigforce.common_ui.ext

import com.gigforce.core.retrofit.GsonExclusionStrategy
import com.google.gson.GsonBuilder
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Type

fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body()!!
}

inline fun <reified T> Response<T>.bodyOrErrorBodyElseThrow(): T {

    return when (code()) {
        in 200..299 -> body()!!
        in 400..499 -> tryConvertingErrorBodyToJson()
        else -> throw HttpException(this)
    }
}

inline fun <reified T> Response<T>.tryConvertingErrorBodyToJson(): T {
    val errorBodyString = errorBody()?.string() ?: throw HttpException(this)

    val gson = GsonBuilder()
        .setExclusionStrategies(GsonExclusionStrategy())
        .setLenient()
        .serializeNulls()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    return gson.fromJson(
        errorBodyString,
        T::class.java
    )
}


fun <T> Response<T>.toException() = HttpException(this)