package com.gigforce.core.datamodels.gigpage

import android.os.Parcelable
import androidx.annotation.Keep
import com.gigforce.core.SimpleDVM
import com.gigforce.core.extensions.toLocalDateTime
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Keep
data class Gig(

    @get:PropertyName("tag")
        @set:PropertyName("tag")
        var tag: String = "",

    @DocumentId
        @get:PropertyName("gigId")
        @set:PropertyName("gigId")
        var gigId: String = "",

    @get:PropertyName("gigerId")
        @set:PropertyName("gigerId")
        var gigerId: String = "",

    @get:PropertyName("title")
        @set:PropertyName("title")
        var title: String = "",

    @get:PropertyName("description")
        @set:PropertyName("description")
        var description: String = "",

    @get:PropertyName("bannerImage")
        @set:PropertyName("bannerImage")
        var bannerImage: String? = null,

    @get:PropertyName("address")
        @set:PropertyName("address")
        var address: String = "",

    @get:PropertyName("geoPoint")
        @set:PropertyName("geoPoint")
        var geoPoint: GeoPoint? = null,

    @get:PropertyName("latitude")
        @set:PropertyName("latitude")
        var latitude: Double? = null,

    @get:PropertyName("longitude")
        @set:PropertyName("longitude")
        var longitude: Double? = null,

    @get:PropertyName("gigAmount")
        @set:PropertyName("gigAmount")
        var gigAmount: Double = 0.0,

    @get:PropertyName("invoiceGenerationDate")
        @set:PropertyName("invoiceGenerationDate")
        var invoiceGenerationDate: Timestamp? = null,

    @get:PropertyName("paymentStatus")
        @set:PropertyName("paymentStatus")
        var paymentStatus: String = "Processing",

    @get:PropertyName("cancellationReason")
        @set:PropertyName("cancellationReason")
        var cancellationReason: String = "",

    @get:PropertyName("companyName")
        @set:PropertyName("companyName")
        var companyName: String? = null,

    @get:PropertyName("companyLogo")
        @set:PropertyName("companyLogo")
        var companyLogo: String? = null,

    @get:PropertyName("startDateTime")
        @set:PropertyName("startDateTime")
        var startDateTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkInBeforeSlot")
        @set:PropertyName("checkInBeforeSlot")
        var checkInBeforeTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkInBeforeBuffer")
        @set:PropertyName("checkInBeforeBuffer")
        var checkInBeforeBufferTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkInAfterBuffer")
        @set:PropertyName("checkInAfterBuffer")
        var checkInAfterBufferTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkInAfterSlot")
        @set:PropertyName("checkInAfterSlot")
        var checkInAfterTime: Timestamp = Timestamp.now(),

    @get:PropertyName("endDateTime")
        @set:PropertyName("endDateTime")
        var endDateTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkOutBeforeSlot")
        @set:PropertyName("checkOutBeforeSlot")
        var checkOutBeforeTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkOutBeforeBuffer")
        @set:PropertyName("checkOutBeforeBuffer")
        var checkOutBeforeBufferTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkOutAfterBuffer")
        @set:PropertyName("checkOutAfterBuffer")
        var checkOutAfterBufferTime: Timestamp = Timestamp.now(),

    @get:PropertyName("checkOutAfterSlot")
        @set:PropertyName("checkOutAfterSlot")
        var checkOutAfterTime: Timestamp = Timestamp.now(),

    @get:PropertyName("agencyContact")
        @set:PropertyName("agencyContact")
        var agencyContact: ContactPerson? = null,

    @get:PropertyName("businessContact")
        @set:PropertyName("businessContact")
        var businessContact: ContactPerson? = null,

    @get:PropertyName("assignedOn")
        @set:PropertyName("assignedOn")
        var assignedOn: Timestamp = Timestamp.now(),

    var checkInBeforeTimeBufferInMins: Long = 60,
    var checkInAfterTimeBufferInMins: Long = 60,
    var checkOutBeforeTimeBufferInMins: Long = 60,
    var checkOutAfterTimeBufferInMins: Long = 60,

    @get:PropertyName("gigStatus")
        @set:PropertyName("gigStatus")
        var gigStatus: String = "upcoming",

    @get:PropertyName("isGigActivated")
        @set:PropertyName("isGigActivated")
        var isGigActivated: Boolean = true, //TODO change this

    @get:PropertyName("isFavourite")
        @set:PropertyName("isFavourite")
        var isFavourite: Boolean = false,

    @get:PropertyName("isGigCompleted")
        @set:PropertyName("isGigCompleted")
        var isGigCompleted: Boolean = false,

    @get:PropertyName("isPaymentDone")
        @set:PropertyName("isPaymentDone")
        var isPaymentDone: Boolean = false,

    @get:PropertyName("isMonthlyGig")
        @set:PropertyName("isMonthlyGig")
        var isMonthlyGig: Boolean = false,

    @get:PropertyName("isFullDay")
        @set:PropertyName("isFullDay")
        var isFullDay: Boolean = false,


    @get:PropertyName("gigOrderId")
        @set:PropertyName("gigOrderId")
        var gigOrderId: String = "",

    @get:PropertyName("profile")
        @set:PropertyName("profile")
        var profile: JobProfile = JobProfile(),

    @get:PropertyName("legalEntity")
        @set:PropertyName("legalEntity")
        var legalEntity: LegalEntity? = null,

    var keywords: List<String> = emptyList(),

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
    var gigResponsibilities: List<String> = listOf(),
    var attendance: GigAttendance? = null,
    var gigContactDetails: GigContactDetails? = null,

    @get:PropertyName("declinedBy")
        @set:PropertyName("declinedBy")
        var declinedBy: String? = null,

    @get:PropertyName("declineReason")
        @set:PropertyName("declineReason")
        var declineReason: String? = null,

    var payoutDetails: String? = null,

    @get:PropertyName("isNewGig")
        @set:PropertyName("isNewGig")
        var isNewGig: Boolean? = null,

    @get:PropertyName("regularisationRequest")
        @set:PropertyName("regularisationRequest")
        var regularisationRequest: GigRegularisationRequest? = null,

    @field:Exclude
        var chatInfo: Map<String, Any>? = null

) {

    @get:Exclude
    @set:Exclude
    var startHour: Int = 0
        get() = startDateTime.toLocalDateTime().hour

    @get:Exclude
    @set:Exclude
    var startMinute: Int = 0
        get() = startDateTime.toLocalDateTime().minute

    @get:Exclude
    @set:Exclude
    var duration: Float = 0.0F
        get() {
            val diffInMilliSecs = endDateTime.toDate().time - startDateTime.toDate().time
            val minutes = TimeUnit.MINUTES.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)
            val hours = TimeUnit.HOURS.convert(diffInMilliSecs, TimeUnit.MILLISECONDS)

            return (hours + (minutes - 60 * hours) / 60.0).toFloat()
        }

    @Exclude
    fun isGigOfToday(): Boolean {

        val gigDate =
                startDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    @Exclude
    fun isGigOfFuture(): Boolean {
        val gigDate =
                startDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    @Exclude
    fun isGigOfPastDay(): Boolean {

        val gigDate =
                startDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
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

            val gigCheckInTime = startDateTime.toLocalDateTime()
            val maxCheckInTime = gigCheckInTime.plusMinutes(checkInAfterTimeBufferInMins)
            if (LocalDateTime.now().isAfter(maxCheckInTime) && isCheckInMarked().not()) {
                //User Has Exceeded max check in time and hasn't marked check-in yet
                return true
            }

            return if (endDateTime != null) {

                val gigCheckOutTime =
                        endDateTime.toDate().toInstant().atZone(ZoneId.systemDefault())
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
                startDateTime.toDate().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusMinutes(checkInBeforeTimeBufferInMins)
        val maxCheckInTime = gigCheckInTime.plusMinutes(checkInAfterTimeBufferInMins)
        val currentTime = LocalDateTime.now()
        val validCheckInTime =
                (currentTime.isAfter(minCheckInTime) && currentTime.isBefore(maxCheckInTime)) || isCheckInMarked()

        return if (endDateTime != null) {

            val gigCheckOutTime =
                    endDateTime.toDate().toInstant().atZone(ZoneId.systemDefault())
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
                startDateTime.toDate().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        val minCheckInTime = gigCheckInTime.minusMinutes(checkInBeforeTimeBufferInMins)
        val currentTime = LocalDateTime.now()

        return minCheckInTime.isAfter(currentTime)
    }

    @Exclude
    fun hasRequestRegularisation(): Boolean {
        return regularisationRequest != null
    }

    @Exclude
    fun getGigTitle(): String {
        return profile.title ?: title
    }

    @Exclude
    fun openNewGig(): Boolean {
        return legalEntity != null
    }

    @Exclude
    fun getFullCompanyName(): String? {

        if (legalEntity != null) {
            return legalEntity?.tradingName ?: legalEntity?.name
        } else {
            return companyName
        }
    }

    @Exclude
    fun getFullCompanyLogo(): String? {

        if (legalEntity != null) {
            return legalEntity?.logo
        } else {
            return companyLogo
        }
    }


    @Exclude
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val obj = other as? Gig
        return (obj?.gigId == gigId)

    }
}


@Parcelize
data class LegalEntity(

        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String? = null,

        @get:PropertyName("logo")
        @set:PropertyName("logo")
        var logo: String? = null,

        @get:PropertyName("name")
        @set:PropertyName("name")
        var name: String? = null,

        @get:PropertyName("tradingName")
        @set:PropertyName("tradingName")
        var tradingName: String? = null
) : Parcelable

@Parcelize
data class JobProfile(

        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String? = null,

        @get:PropertyName("activationCode")
        @set:PropertyName("activationCode")
        var activationCode: String? = null,

        @get:PropertyName("title")
        @set:PropertyName("title")
        var title: String? = null
) : Parcelable


@Parcelize
data class ContactPerson(

        @get:PropertyName("uid")
        @set:PropertyName("uid")
        var uid: String? = null,

        @get:PropertyName("name")
        @set:PropertyName("name")
        var name: String? = null,

        @get:PropertyName("designation")
        @set:PropertyName("designation")
        var designation: String? = null,

        @get:PropertyName("profilePicture")
        @set:PropertyName("profilePicture")
        var profilePicture: String? = null,

        @get:PropertyName("primary_no")
        @set:PropertyName("primary_no")
        var contactNumber: String? = null,

        @get:PropertyName("secondary_no")
        @set:PropertyName("secondary_no")
        var secondaryContactNo: String? = null,

        @get:PropertyName("company_name")
        @set:PropertyName("company_name")
        var companyName: String? = null
) : Parcelable