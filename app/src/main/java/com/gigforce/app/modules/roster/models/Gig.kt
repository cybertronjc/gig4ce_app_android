package com.gigforce.app.modules.roster.models

import android.media.tv.TvView
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.math.RoundingMode.valueOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Keep
data class Gig (
    var tag: String = "",
    @DocumentId
    var gigId: String = "",
    var gigerId: String = "",
    var gigAmount: Int = 0,
    var startDateTime: Timestamp? = null,
    var endDateTime: Timestamp? = null,
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
    //var duration: Float = 0.0F, // consider changing to end date time
    var gigRating: Float = 0.0F,
    var attendance: GigAttendance? = null
): BaseFirestoreDataModel(tableName = "Gigs") {
    init {

    }

//    var startDateTime: LocalDateTime? = null
//        @RequiresApi(Build.VERSION_CODES.O)
//        get() = LocalDateTime.of(year, month, date, startHour, startMinute)

    var startHour: Int = 0
        get() = startDateTime!!.toDate().hours

    var startMinute: Int = 0
        get() = startDateTime!!.toDate().minutes

    var duration: Float = 0.0F
        get() {
            val diffInMilliSecs = endDateTime!!.toDate().time - startDateTime!!.toDate().time
            val minutes = TimeUnit.MINUTES.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)
            val hours = TimeUnit.HOURS.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)

            return (hours + (minutes - 60*hours) / 60.0).toFloat()
        }

    fun isPastGig(): Boolean {
        if (isGigOfPast())
            return true

        if (isGigOfToday()) {
            if (isCheckInAndCheckOutMarked())
                return true

            return if (endDateTime != null) {

                val gigCheckOutTime =
                    endDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                val maxCheckOutTime = gigCheckOutTime.plusHours(4) //4 Hour window for checkout after gig time expires
                LocalDateTime.now().isAfter(maxCheckOutTime)
            } else {
                false // If end time not given gig will be considered full day gig (present gig)
            }
        } else return false
    }

    fun isPresentGig(): Boolean {

        if (isCheckInAndCheckOutMarked())
            return false

        val gigCheckInTime =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusHours(1)
        val currentTime = LocalDateTime.now()
        val validCheckInTime = minCheckInTime.isBefore(currentTime)

        return if (endDateTime != null) {

            val gigCheckOutTime =
                endDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            val maxCheckOutTime = gigCheckOutTime.plusHours(4)
            currentTime.isBefore(maxCheckOutTime) && validCheckInTime
        } else {
            isGigOfToday() && validCheckInTime
        }
    }

    fun isUpcomingGig(): Boolean {

        val gigCheckInTime =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusHours(1)
        val currentTime = LocalDateTime.now()

        return minCheckInTime.isAfter(currentTime)
    }

    fun isCheckInMarked(): Boolean {
        return attendance?.checkInTime != null
    }

    fun isCheckOutMarked(): Boolean {
        return attendance?.checkOutTime != null
    }

    fun isCheckInAndCheckOutMarked(): Boolean {
        return isCheckInMarked() && isCheckOutMarked()
    }

    private fun isGigOfToday(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    private fun isGigOfFuture(): Boolean {
        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    private fun isGigOfPast(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isBefore(currentDate)
    }

}

