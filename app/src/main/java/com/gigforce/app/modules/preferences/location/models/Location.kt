package com.gigforce.app.modules.preferences.location.models

class Location(
    var isVerified: Boolean = false,
    var currentAddress: String? = null,
    var permanentAddress: String? = null,
    var locationPreference: ArrayList<String>? = null
) {
    fun getTotal(): Float {
        // TODO
        return 0F
    }

}