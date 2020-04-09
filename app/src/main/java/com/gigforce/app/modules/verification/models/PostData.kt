package com.gigforce.app.modules.verification.models

import com.squareup.moshi.Json

//data class PostBody(
//    @SerializedName("task_id") val task_id: Long,
//    @SerializedName("group_id") val group_id: String,
//    @SerializedName("data") val data: String;
//)

class PostBody(){
    companion object{
        lateinit var task_id: String;
        lateinit var group_id: String;
        lateinit var data: String;
    }
    lateinit var task_id: String;
    lateinit var group_id: String;
    lateinit var data: String;
}