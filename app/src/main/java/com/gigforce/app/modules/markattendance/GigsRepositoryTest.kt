package com.gigforce.app.modules.markattendance

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

class GigsRepositoryTest : BaseFirestoreDBRepository() {

    override fun getCollectionName() = "Gigs"

    fun markAttendance(markAttendance : MarkAttendance) {
        getCollectionReference().document("gigId1").update(markAttendance.tableName, markAttendance)
    }
}

class MarkAttendance : BaseFirestoreDataModel {
    var checkInMarked: Boolean = false
    var checkInTime : Date
    var checkInLat: Double = 0.0
    var checkInLong: Double = 0.0
    var checkInImage: String = ""
    var checkInAddress : String = ""
    var checkOutMarked: Boolean = false
    var checkOutTime : Date? = null
    var checkOutLat: Double = 0.0
    var checkOutLong: Double = 0.0
    var checkOutImage: String = ""
    var checkOutAddress : String = ""

    constructor(
        checkInMarked: Boolean = false,
        checkInTime : Date,
        checkInLat: Double ,
        checkInLong: Double,
        checkInImage: String,
        checkInAddress : String
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
        checkOutTime : Date,
        checkOutLat: Double,
        checkOutLong: Double,
        checkOutImage: String
    ) {
        this.checkOutMarked = checkOutMarked
        this.checkOutTime = checkOutTime
        this.checkOutLat = checkOutLat
        this.checkOutLong = checkOutLong
        this.checkOutImage = checkOutImage
    }
}
