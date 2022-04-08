package com.gigforce.giger_gigs.travelling_info

data class TravellingResponseDM(
    val name: String? = "",
    val mobile: String? = "",
    val empCode: String? = "",
    val totalDistance: Double? = 0.0,
    val date: String? = "",
    val totalSlots: Long? = 0,
    val checkInCount: Long? = 0,
    val checkOutCount: Long? = 0,
    val details: ArrayList<TravellingDetailInfoModel>
)