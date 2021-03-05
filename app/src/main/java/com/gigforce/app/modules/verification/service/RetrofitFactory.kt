package com.gigforce.app.modules.verification.service

import com.gigforce.core.AppConstants
import com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi
import com.gigforce.core.retrofit.IdfyApiAadhaar
import com.gigforce.core.retrofit.IdfyApiDL
import com.gigforce.core.retrofit.IdfyApiPAN
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    val gsonConverter: GsonConverterFactory
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
    private val authInterceptor = Interceptor { chain ->
        val newUrl = chain.request().url()
            .newBuilder()
            .addQueryParameter("account-id", AppConstants.idfyAcid)
            .addQueryParameter("api-key", AppConstants.idfyApiKey)
            .addQueryParameter("Content-Type", "application/json")
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        chain.proceed(newRequest)
    }
    //Not logging the authkey if not debug
    private val client =
        OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()









    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    // Since its a factory, we can have several api calls as one of them below:
    fun idfyApiCallAD() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(IdfyApiAadhaar::class.java)

    fun idfyApiCallDL() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(IdfyApiDL::class.java)

    fun idfyApiCallPAN() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(IdfyApiPAN::class.java)

    fun createUserAccEnrollmentAPi() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(CreateUserAccEnrollmentAPi::class.java)
}