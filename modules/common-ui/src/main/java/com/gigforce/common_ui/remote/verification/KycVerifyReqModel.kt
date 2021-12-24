package com.gigforce.common_ui.remote.verification

data class KycVerifyReqModel (

    val type : String,
    val uId : String,
    val data : List<Data>
)

data class Data (
    val type : String,
    val value : String
)
