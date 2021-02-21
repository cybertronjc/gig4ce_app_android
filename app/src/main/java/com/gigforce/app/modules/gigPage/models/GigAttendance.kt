package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import java.io.Serializable
import java.util.*

@Keep
class GigAttendance : BaseFirestoreDataModel, Serializable {

    var checkInMarked: Boolean = false
    var checkInTime: Date? = null
    var checkInLat: Double? = null
    var checkInLong: Double? = null
    var checkInImage: String? = null
    var checkInAddress: String = ""
    var checkOutMarked: Boolean = false
    var checkOutTime: Date? = null
    var checkOutLat: Double? = null
    var checkOutLong: Double? = null
    var checkOutImage: String? = null
    var checkOutAddress: String = ""

    constructor() : super("attendance") {}

    constructor(
        checkInMarked: Boolean = false,
        checkInTime: Date,
        checkInLat: Double,
        checkInLong: Double,
        checkInImage: String,
        checkInAddress: String
    ) : super("attendance") {
        this.checkInMarked = checkInMarked
        this.checkInTime = checkInTime
        this.checkInLat = checkInLat
        this.checkInLong = checkInLong
        this.checkInImage = checkInImage
        this.checkInAddress = checkInAddress
    }

    fun setCheckout(
        checkOutMarked: Boolean,
        checkOutTime: Date,
        checkOutLat: Double,
        checkOutLong: Double,
        checkOutImage: String,
        checkOutAddress: String
    ) {
        this.checkOutMarked = checkOutMarked
        this.checkOutTime = checkOutTime
        this.checkOutLat = checkOutLat
        this.checkOutLong = checkOutLong
        this.checkOutImage = checkOutImage
        this.checkOutAddress = checkOutAddress
    }
}