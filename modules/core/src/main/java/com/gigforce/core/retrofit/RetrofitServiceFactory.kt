package com.gigforce.core.retrofit


import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitServiceFactory @Inject constructor(
    private val buildConfig: IBuildConfig,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) {
    private val retrofit: Retrofit

    init {
        val moshi = makeMoshi()
        val okHttpClient = makeOkHttpClient()
        retrofit = makeRetrofit(
            buildConfig.baseUrl,
            okHttpClient,
            moshi
        )
    }

    private fun makeRetrofit(baseUrl: String, okHttpClient: OkHttpClient, moshi: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(moshi))
            .build()
    }


    private fun addNecessaryInfoToHeadersAndQueryParam(
        originalRequest: Request
    ): Request {
        val requestBuilder = originalRequest.newBuilder()

        //Add Common Headers params to all headers
        prepareCommonHeaders().forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        //Adding common Query Params
        val originalHttpUrl = originalRequest.url()
        val urlBuilder = originalHttpUrl.newBuilder()
        prepareCommonQueryParams().forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        requestBuilder.url(urlBuilder.build())

        return requestBuilder.build()
    }

    private fun prepareCommonHeaders(): Map<String, String> = mutableMapOf<String, String>().apply {
        put("Source", "android")
        put("AndroidAppVersion", buildConfig.appVersion)
        put("AndroidAppFlavour", buildConfig.appFlavour)
        put("AndroidAppBuildType", buildConfig.appBuildType)

        firebaseAuthStateListener.getCurrentSignInInfo()?.let {
            put("LoggedInUserUid", it.uid)
        }
    }

    private fun prepareCommonQueryParams(): Map<String, String> {
        return emptyMap()
    }

    private fun makeOkHttpClient(
    ): OkHttpClient {

        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(NetworkCallNecessaryInfoAddingIntercepter())

        if (buildConfig.debugBuild) {
            okHttpBuilder.addInterceptor(makeNetworkCallLoggingInterceptor())
        }

        return okHttpBuilder.build()
    }

    private fun makeMoshi(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()

    }

    private fun makeNetworkCallLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .apply { this.level = HttpLoggingInterceptor.Level.BODY }
    }

    fun <T> prepareService(service: Class<T>): T {
        return retrofit.create(service)
    }

    private inner class NetworkCallNecessaryInfoAddingIntercepter : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val modifiedRequest = addNecessaryInfoToHeadersAndQueryParam(chain.request())
            return chain.proceed(modifiedRequest)
        }
    }
}