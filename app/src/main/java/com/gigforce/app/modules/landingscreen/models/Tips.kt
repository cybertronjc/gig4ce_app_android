package com.gigforce.app.modules.landingscreen.models

data class Tip(
    val title : String,
    val subTitle : String,
    val whereToRedirect : Int,
    val intentExtraMap : Map<String, Any> = mapOf()
)