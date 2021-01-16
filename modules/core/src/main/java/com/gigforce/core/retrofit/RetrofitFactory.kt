package com.gigforce.core.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    private val gsonConverter: GsonConverterFactory
        get() {
            return GsonConverterFactory
                .create(
                    GsonBuilder()
                        .setLenient()
                        .disableHtmlEscaping()
                        .create()
                )
        }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client =
        OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()


    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://gigforce.in/")
            .addConverterFactory(gsonConverter)
            .client(client)
            .build()
    }


    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}