package com.gigforce.app.modules.landingscreen.models

data class WorkOrder(
    var business_id: String? = null,
    var business_name: String? = null,
    var faqs: Faqs? = null,
    var requiredLessons: RequiredLessons? = null,
    var requirments: ArrayList<Requirements>? = null,
    var responsibilties: ArrayList<String>? = null,
    var work_order_icon: String? = null,
    var work_order_title: String? = null,
    var profile_name: String? = null,
    var profile_id: String? = null,
    var locations: ArrayList<Locations>? = null,
    var payoutNote: String? = null,
    var queries: ArrayList<Queries>? = null,
    var id: String? = null
)