package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.models.IdfydataResponse
import com.gigforce.app.modules.verification.models.PlaceholderPhotos
import com.gigforce.app.modules.verification.models.PlaceholderPosts
import com.gigforce.app.modules.verification.models.PlaceholderUsers
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface PlaceholderApi{

    @GET("/posts")
    fun getPosts() : Deferred<Response<List<PlaceholderPosts>>>

    @GET("/users")
    fun getUsers() : Deferred<Response<List<PlaceholderUsers>>>

    @GET("/photos")
    fun getPhotos() : Deferred<Response<List<PlaceholderPhotos>>>
}