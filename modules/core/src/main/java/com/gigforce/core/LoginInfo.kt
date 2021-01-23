package com.gigforce.core

class LoginInfo() {

    var isLoggedIn:Boolean = false

    var uid:String? = null
    var profilePicPath:String? = null
    var profileName: String? = null

    var isAmbassador:Boolean? = null

    fun onSignOut(){
        isLoggedIn = false

        uid = null
        profilePicPath = null
        profileName = null
        isAmbassador = null
    }
}

interface ILoginInfoProvider {
    fun provideLoginInfo():LoginInfo
}