package com.gigforce.app.utils.network

import com.gigforce.app.modules.chatmodule.remote.SyncContactsService
import com.gigforce.core.AppConstants
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

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

    //Not logging the authkey if not debug
    private val client =
        OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()


//    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
//        .client(client)
//        .baseUrl(baseUrl)
//        .addConverterFactory(MoshiConverterFactory.create())
//        .addCallAdapterFactory(CoroutineCallAdapterFactory())
//        .build()
    fun retrofit(baseUrl: String): NetworkCalls = Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(NetworkCalls::class.java)




    fun generateSyncContactsService() = Retrofit.Builder()
        .baseUrl(AppConstants.IDFY_BASE_URL)
        .addConverterFactory(gsonConverter)
        .client(client)
        .build()
        .create(SyncContactsService::class.java)
}