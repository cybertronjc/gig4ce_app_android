package com.gigforce.app.modules.onboarding.models

data class UserData(var name:String)

object Supplier {

    val users = listOf<UserData>(
        UserData("pd") ,UserData("pd"),
        UserData("pd"), UserData("pd"),
        UserData("pd"), UserData("pd"),
        UserData("pd"), UserData("pd"),
        UserData("pd"), UserData("pd")
    )
}