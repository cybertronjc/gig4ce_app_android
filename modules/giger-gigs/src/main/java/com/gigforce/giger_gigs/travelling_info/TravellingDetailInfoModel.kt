package com.gigforce.giger_gigs.travelling_info

data class TravellingDetailInfoModel(
    val checkin_time: String? = "",
    val checkout_time: String? = "",
    val checkin_location: String? = "",
    val checkout_location: String? = "",
    val checkin_latitude: String? = "",
    val checkin_longitude: String? = "",
    val checkout_latitude: String? = "",
    val checkout_longitude: String? = ""
)