package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep

@Keep
data class GigAttendance(
    var checkInMarked: Boolean,
    var checkInLat: Double?,
    var checkInLong: Double?,
    var checkInImage: String?,
    var checkOutMarked: Boolean,
    var checkOutLat: Double?,
    var checkOutLong: Double?,
    var checkOutImage: String?
)