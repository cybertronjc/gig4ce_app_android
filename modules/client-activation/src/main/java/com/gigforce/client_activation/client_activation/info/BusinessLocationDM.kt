package com.gigforce.client_activation.client_activation.info

data class BusinessLocationDM(var id : String = "",val state: State?=null, val name: String?=null){
}

data class State(
    val country_code: String?=null,
    val id: String?=null,
    val name: String?=null
)