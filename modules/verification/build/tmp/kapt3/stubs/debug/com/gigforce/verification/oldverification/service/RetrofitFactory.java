package com.gigforce.verification.oldverification.service;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000e\u001a\n \u0007*\u0004\u0018\u00010\u000f0\u000fJ\u000e\u0010\u0010\u001a\n \u0007*\u0004\u0018\u00010\u00110\u0011J\u000e\u0010\u0012\u001a\n \u0007*\u0004\u0018\u00010\u00130\u0013J\u000e\u0010\u0014\u001a\n \u0007*\u0004\u0018\u00010\u00150\u0015J\u000e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\b\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/gigforce/verification/oldverification/service/RetrofitFactory;", "", "()V", "authInterceptor", "Lokhttp3/Interceptor;", "client", "Lokhttp3/OkHttpClient;", "kotlin.jvm.PlatformType", "gsonConverter", "Lretrofit2/converter/gson/GsonConverterFactory;", "getGsonConverter", "()Lretrofit2/converter/gson/GsonConverterFactory;", "loggingInterceptor", "Lokhttp3/logging/HttpLoggingInterceptor;", "createUserAccEnrollmentAPi", "Lcom/gigforce/core/retrofit/CreateUserAccEnrollmentAPi;", "idfyApiCallAD", "Lcom/gigforce/core/retrofit/IdfyApiAadhaar;", "idfyApiCallDL", "Lcom/gigforce/core/retrofit/IdfyApiDL;", "idfyApiCallPAN", "Lcom/gigforce/core/retrofit/IdfyApiPAN;", "retrofit", "Lretrofit2/Retrofit;", "baseUrl", "", "verification_debug"})
public final class RetrofitFactory {
    private static final okhttp3.logging.HttpLoggingInterceptor loggingInterceptor = null;
    private static final okhttp3.Interceptor authInterceptor = null;
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.oldverification.service.RetrofitFactory INSTANCE = null;
    
    @org.jetbrains.annotations.NotNull()
    public final retrofit2.converter.gson.GsonConverterFactory getGsonConverter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final retrofit2.Retrofit retrofit(@org.jetbrains.annotations.NotNull()
    java.lang.String baseUrl) {
        return null;
    }
    
    public final com.gigforce.core.retrofit.IdfyApiAadhaar idfyApiCallAD() {
        return null;
    }
    
    public final com.gigforce.core.retrofit.IdfyApiDL idfyApiCallDL() {
        return null;
    }
    
    public final com.gigforce.core.retrofit.IdfyApiPAN idfyApiCallPAN() {
        return null;
    }
    
    public final com.gigforce.core.retrofit.CreateUserAccEnrollmentAPi createUserAccEnrollmentAPi() {
        return null;
    }
    
    private RetrofitFactory() {
        super();
    }
}