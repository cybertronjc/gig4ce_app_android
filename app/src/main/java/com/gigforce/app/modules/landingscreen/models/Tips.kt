package com.gigforce.app.modules.landingscreen.models

data class Tip(
    val key: String = "",
    val title: String = "",
    val subTitle: String = "",
    val whereToRedirect: Int = -1,
    val skipped: Boolean = false,
    var tip_id: Int = 0,
    val intentExtraMap: Map<String, Any> = mapOf()
) {
    override fun equals(other: Any?): Boolean {
        val that = other as Tip
        return key == that.key
    }
}