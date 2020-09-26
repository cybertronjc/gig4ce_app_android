package com.gigforce.app.modules.landingscreen.models

data class Role(
    var id: String? = "",
    var about: String = "",
    var job_description: ArrayList<String>? = null,
    var payments_and_benefits: ArrayList<String>? = null,
    var requirements: ArrayList<String>? = null,
    var role_image: String? = null,
    var role_title: String? = null,
    var top_locations: ArrayList<String>? = null
) {
}