package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

@Keep
data class GigAttendance(
    var checkInMarked: Boolean = false,
    var checkInTime : Timestamp? = null,
    var checkInLat: Double? = null,
    var checkInLong: Double? = null,
    var checkInImage: String? = null,
    var checkOutMarked: Boolean = false,
    var checkOutTime : Timestamp? = null,
    var checkOutLat: Double? = null,
    var checkOutLong: Double? = null,
    var checkOutImage: String? = null
)