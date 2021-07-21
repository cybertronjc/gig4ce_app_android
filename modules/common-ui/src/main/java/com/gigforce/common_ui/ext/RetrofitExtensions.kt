package com.gigforce.common_ui.ext

import retrofit2.HttpException
import retrofit2.Response

inline fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body()!!
}

inline fun <T> Response<T>.toException() = HttpException(this)