package com.gigforce.app.modules.preferences.location.models

class LocationModel(
    var isVerified: Boolean = false,
    var currentAddress: String = "",
    var permanentAddress: String = "",
    var locationPreference: ArrayList<String> =ArrayList<String>()
)
