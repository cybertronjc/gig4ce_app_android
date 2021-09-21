package com.gigforce.common_ui.viewdatamodels.client_activation

data class Role(
    var id: String? = "",
    var about: String = "",
    var job_description: ArrayList<String>? = null,
    var payments_and_benefits: ArrayList<String>? = null,
    var requirements: ArrayList<String>? = null,
    var role_image: String? = null,
    var role_title: String? = null,
    var top_locations: ArrayList<String>? = null,
    var isMarkedAsInterest: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        val obj = other as Role
        return obj.id.equals(id)
    }
}