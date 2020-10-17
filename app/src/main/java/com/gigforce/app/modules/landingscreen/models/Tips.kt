package com.gigforce.app.modules.landingscreen.models

data class Tip(
    val title: String,
    val subTitle: String,
    val whereToRedirect: Int,
    val skipped: Boolean = false,
    var tip_id: Int = 0,
    val intentExtraMap: Map<String, Any> = mapOf()
)