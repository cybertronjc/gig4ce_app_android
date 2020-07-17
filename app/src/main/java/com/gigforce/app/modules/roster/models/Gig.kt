package com.gigforce.app.modules.roster.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.Timestamp
import java.math.RoundingMode.valueOf
import java.time.LocalDateTime

data class Gig (
    var tag: String = "",
    var gigId: String = "",
    var gigerId: String = "",
    var gigAmount: Int = 0,
    var startDateTime: Timestamp? = null,
    var title: String = "",
    var gigStatus: String = "upcoming",
    @field:JvmField
    var isGigCompleted: Boolean = false,
    @field:JvmField
    var isPaymentDone: Boolean = false,
    @field:JvmField
    var isFullDay: Boolean = false,
    var date: Int = 0,
    var month: Int = 0,
    var year: Int = 0,
    var duration: Float = 0.0F, // consider changing to end date time
    var gigRating: Float = 0.0F
): BaseFirestoreDataModel(tableName = "Gigs") {
    init {
//            startDateTime ?.let {
//                startHour = startDateTime!!.toDate().hours
//                startMinute = startDateTime!!.toDate().minutes
//            }
        }

//    var startDateTime: LocalDateTime? = null
//        @RequiresApi(Build.VERSION_CODES.O)
//        get() = LocalDateTime.of(year, month, date, startHour, startMinute)

    var startHour: Int = 0
        get() = startDateTime!!.toDate().hours

    var startMinute: Int = 0
        get() = startDateTime!!.toDate().minutes
}

