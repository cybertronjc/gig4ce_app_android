package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.gigforce.app.core.toLocalDateTime
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Keep
data class Gig(
    var tag: String = "",
    @DocumentId var gigId: String = "",
    var gigerId: String = "",
    var title: String = "",
    var bannerImage: String? = null,
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,

    var gigAmount: Double = 0.0,
    var invoiceGenerationDate: Timestamp? = null,
    var paymentStatus: String = "Processing",

    var startDateTime: Timestamp? = null,
    var endDateTime: Timestamp? = null,

    var checkInBeforeTimeBufferInMins: Long = 60,
    var checkInAfterTimeBufferInMins: Long = 60,
    var checkOutBeforeTimeBufferInMins: Long = 60,
    var checkOutAfterTimeBufferInMins: Long = 60,

    var gigStatus: String = "upcoming",
    var companyLogo: String? = null,
    var companyName: String? = null,
    @field:JvmField var isGigActivated: Boolean = true, //TODO change this
    @field:JvmField var isFavourite: Boolean = false,
    @field:JvmField var isGigCompleted: Boolean = false,
    @field:JvmField var isPaymentDone: Boolean = false,
    @field:JvmField var isMonthlyGig: Boolean = false,
    @field:JvmField var isFullDay: Boolean = false,

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
    var gigContactDetails: GigContactDetails? = null,

    var declinedBy: String? = null,
    var declineReason: String? = null
) {

    @get:Exclude
    @set:Exclude
    var startHour: Int = 0
        get() = startDateTime!!.toLocalDateTime().hour

    @get:Exclude
    @set:Exclude
    var startMinute: Int = 0
        get() = startDateTime!!.toLocalDateTime().minute

    @get:Exclude
    @set:Exclude
    var duration: Float = 0.0F
        get() {
            val diffInMilliSecs = endDateTime!!.toDate().time - startDateTime!!.toDate().time
            val minutes = TimeUnit.MINUTES.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)
            val hours = TimeUnit.HOURS.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)

            return (hours + (minutes - 60 * hours) / 60.0).toFloat()
        }

    @Exclude
    fun isGigOfToday(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    @Exclude
    fun isGigOfFuture(): Boolean {
        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    @Exclude
    fun isGigOfPastDay(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isBefore(currentDate)
    }

    @Exclude
    fun isCheckInAndCheckOutMarked(): Boolean {
        return isCheckInMarked() && isCheckOutMarked()
    }

    @Exclude
    fun isCheckInOrCheckOutMarked(): Boolean {
        return isCheckInMarked() || isCheckOutMarked()
    }

    @Exclude
    fun isCheckInMarked(): Boolean {
        return attendance?.checkInTime != null
    }

    @Exclude
    fun isCheckOutMarked(): Boolean {
        return attendance?.checkOutTime != null
    }

    @Exclude
    fun isPastGig(): Boolean {
        if (isGigOfPastDay())
            return true

        if (isGigOfToday()) {
            if (isCheckInAndCheckOutMarked())
                return true

            val gigCheckInTime = startDateTime!!.toLocalDateTime()
            val maxCheckInTime = gigCheckInTime.plusMinutes(checkInAfterTimeBufferInMins)
            if (LocalDateTime.now().isAfter(maxCheckInTime) && isCheckInMarked().not()) {
                //User Has Exceeded max check in time and hasn't marked check-in yet
                return true
            }

            return if (endDateTime != null) {

                val gigCheckOutTime =
                    endDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                val maxCheckOutTime =
                    gigCheckOutTime.plusMinutes(checkOutAfterTimeBufferInMins) //1 Hour window for checkout after gig time expires
                LocalDateTime.now().isAfter(maxCheckOutTime)
            } else {
                false // If end time not given gig will be considered full day gig (present gig)
            }
        } else return false
    }

    @Exclude
    fun isPresentGig(): Boolean {

        if (isCheckInAndCheckOutMarked())
            return false

        val gigCheckInTime =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusMinutes(checkInBeforeTimeBufferInMins)
        val maxCheckInTime = gigCheckInTime.plusMinutes(checkInAfterTimeBufferInMins)
        val currentTime = LocalDateTime.now()
        val validCheckInTime =
            (currentTime.isAfter(minCheckInTime) && currentTime.isBefore(maxCheckInTime)) || isCheckInMarked()

        return if (endDateTime != null) {

            val gigCheckOutTime =
                endDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

//            if (gigCheckOutTime.isBefore(currentTime)) {
//                return false
//            }

            val maxCheckOutTime = gigCheckOutTime.plusMinutes(checkOutAfterTimeBufferInMins)
            currentTime.isBefore(maxCheckOutTime) && validCheckInTime
        } else {
            isGigOfToday() && validCheckInTime
        }
    }

    @Exclude
    fun isUpcomingGig(): Boolean {

        val gigCheckInTime =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusMinutes(checkInBeforeTimeBufferInMins)
        val currentTime = LocalDateTime.now()

        return minCheckInTime.isAfter(currentTime)
    }

}