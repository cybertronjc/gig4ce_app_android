package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.models.Idfydata
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface PlaceholderApi{

    @GET("/posts")
    fun getPosts() : Deferred<Response<List<Idfydata>>>

    @GET("/users")
    fun getUsers() : Deferred<Response<List<Idfydata>>>

    @GET("/photos")
    fun getPhotos() : Deferred<Response<List<Idfydata>>>
}