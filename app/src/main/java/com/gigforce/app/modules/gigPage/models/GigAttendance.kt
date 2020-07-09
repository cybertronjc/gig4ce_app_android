package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep

@Keep
data class GigAttendance(
    var checkInMarked: Boolean = false,
    var checkInLat: Double? = null,
    var checkInLong: Double? = null,
    var checkInImage: String? = null,
    var checkOutMarked: Boolean = false,
    var checkOutLat: Double? = null,
    var checkOutLong: Double? = null,
    var checkOutImage: String? = null
)