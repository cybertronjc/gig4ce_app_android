package com.gigforce.core.retrofit

import com.gigforce.core.AppConstants
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {

    private val gsonConverter: GsonConverterFactory
        get() {
            return GsonConverterFactory
                .create(
                    GsonBuilder()
                        .setLenient()
                        .disableHtmlEscaping()
                        .serializeNulls()
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
            .connectTimeout(2, TimeUnit.MINUTES) // connect timeout
            .readTimeout(2, TimeUnit.MINUTES)    // read timeout
            .writeTimeout(2, TimeUnit.MINUTES)   // write timeout
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
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


    //----------------------------------------------
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

    fun generatePaySlipService() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(GeneratePaySlipService::class.java)
}