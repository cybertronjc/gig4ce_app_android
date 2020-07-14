package com.gigforce.app.modules.gigerVerfication

import com.gigforce.app.modules.verification.AppConstants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    val gsonConverter: GsonConverterFactory
        get() {
            return GsonConverterFactory
                .create(
                    GsonBuilder()
                        .setLenient()
                        .disableHtmlEscaping()
                        .create())!!
        }

    private val loggingInterceptor =  HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    //Not logging the authkey if not debug
    private val client =
        OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()

    fun retrofit(baseUrl : String) : Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(baseUrl)
        .addConverterFactory(gsonConverter)
        .build()

    // Since its a factory, we can have several api calls as one of them below:
    fun verificationThirdPartyService() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(VerficationThirdPartyServices::class.java)


}