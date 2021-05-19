package com.gigforce.profile.models

import com.google.firebase.firestore.PropertyName

data class State(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = ""
){

    override fun toString(): String {
        return name
    }
}

data class City(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("state_code")
    @set:PropertyName("state_code")
    var stateCode: String = "",

    @get:PropertyName("cityCode")
    @set:PropertyName("cityCode")
    var cityCode: String = "",

    @get:PropertyName("sub_location")
    @set:PropertyName("sub_location")
    var subLocation: List<String> = emptyList(),

    @get:PropertyName("subLocationFound")
    @set:PropertyName("subLocationFound")
    var subLocationFound:  Boolean = false,

){

    override fun toString(): String {
        return name
    }
}

data class CityWithImage(

    var id: String = "",
    var name: String = "",
    var state_code: String = "",
    var cityCode: String ="",
    var image: Int = -1,
    var index: Int = -1,
    var icon: String = "",
    var subLocationFound: Boolean = false,
){

    override fun toString(): String {
        return name
    }
}

data class SubCity(

    var name: String = "",
    var state_code: String = "",
    var cityCode: String ="",
    var index: Int = -1,
){

    override fun toString(): String {
        return name
    }
}
