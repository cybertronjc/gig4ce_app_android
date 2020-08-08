package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Keep
data class Gig(
    var gigId: String = "",
    var gigerId: String = "",
    var gigAmount: Double = 0.0,
    var title: String = "",
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var startDateTime: Timestamp? = null,
    var endDateTime: Timestamp? = null,
    var gigStatus: String = "upcoming",
    var companyLogo: String? = null,
    var companyName: String? = null,
    var contactNo: String? = null,
    @field:JvmField var isGigActivated: Boolean = true, //TODO change this
    @field:JvmField var isFavourite: Boolean = false,
    @field:JvmField var isGigCompleted: Boolean = false,
    @field:JvmField var isPaymentDone: Boolean = false,
    var duration: Float = 0.0F,
    var gigRating: Float = 0.0F,
    var gigUserFeedback: String? = null,
    var gigUserFeedbackAttachments: List<String> = emptyList(),
    var locationPictures: List<String> = emptyList(),

    var ratingUserReceived: Float = -1.0F,
    var feedbackUserReceived: String? = null,
    var ratingUserReceivedAttachments: List<String> = emptyList(),

    var gigType: String? = null,
    var gigHighlights: List<String> = emptyList(),
    var gigRequirements: List<String> = emptyList(),
    var attendance: GigAttendance? = null,
    var gigContactDetails: GigContactDetails? = null
) : Serializable {

    fun isGigOfToday(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    fun isGigOfFuture(): Boolean {
        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    fun isGigOfPast(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isBefore(currentDate)
    }

    fun isCheckInAndCheckOutMarked(): Boolean {
        return isCheckInMarked() && isCheckOutMarked()
    }

    fun isCheckInOrCheckOutMarked(): Boolean {
        return isCheckInMarked() || isCheckOutMarked()
    }

    fun isCheckInMarked(): Boolean {
        return attendance?.checkInTime != null
    }

    fun isCheckOutMarked(): Boolean {
        return attendance?.checkOutTime != null
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
                val maxCheckOutTime =
                    gigCheckOutTime.plusHours(4) //4 Hour window for checkout after gig time expires
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

            if (gigCheckOutTime.isBefore(currentTime)) {
                return false
            }

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

}