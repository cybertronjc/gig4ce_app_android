package com.gigforce.client_activation.client_activation.info.hubform

data class BusinessLocationDM(
    var id: String = "",
    val state: State? = null,
    val name: String? = null,
    val city: City? = null
)

data class City(
    val id: String? = "",
    val name: String? = "",
    val state_code: String? = ""
)

data class State(
    val country_code: String? = null,
    val id: String? = null,
    val name: String? = null
)