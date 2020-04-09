package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.models.Idfydata
import com.gigforce.app.modules.verification.models.IdfydataResponse
import com.squareup.moshi.Json
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

interface IdfyApi{
//
//    {"task_id": "74f4c926-250c-43ca-9c53-453e87ceacd2","group_id": "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f",
//         "data": {"document1": "'+encoded_string1.decode('utf-8')+'",
//        "document2": "'+encoded_string2.decode('utf-8')+'","consent": "yes","details": {}}}

    @FormUrlEncoded
    @POST("async/extract/ind_aadhaar")
    fun postAadhar(
    @Field("task_id") task_id:String,
    @Field("group_id") group_id:String,
    @Field("data") data: String): Call<IdfydataResponse>;

    @POST("sync/extract/ind_aadhaar")
    fun postAadhar(
        @Body data: String
    ): Call<Idfydata>;
}