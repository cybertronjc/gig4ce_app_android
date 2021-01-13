package com.gigforce.app.modules.ambassador_user_enrollment.models

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
    var stateCode: String = ""
){

    override fun toString(): String {
        return name
    }
}