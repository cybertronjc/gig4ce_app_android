package com.gigforce.common_ui.viewdatamodels.gig

import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigAttendance
import com.gigforce.core.datamodels.gigpage.JobProfile
import com.gigforce.core.extensions.toDate
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class GigApiModel(

    @field:SerializedName("checkInAfterSlot")
    val checkInAfterSlot: String,

    @field:SerializedName("gigRating")
    val gigRating: Int? = null,

    @field:SerializedName("checkInAfterBuffer")
    val checkInAfterBuffer: String,

    @field:SerializedName("gigerId")
    val gigerId: String? = null,

    @field:SerializedName("gigUserFeedback")
    val gigUserFeedback: String? = null,

    @field:SerializedName("businessId")
    val businessId: String? = null,

    @field:SerializedName("gigDate")
    val gigDate: String? = null,

    @field:SerializedName("legalEntity")
    val legalEntity: LegalEntity? = null,

    @field:SerializedName("isMonthlyGig")
    val isMonthlyGig: Boolean? = null,

    @field:SerializedName("gigUserFeedbackAttachments")
    val gigUserFeedbackAttachments: List<Any?>? = null,

    @field:SerializedName("assignOn")
    val assignOn: String,

    @field:SerializedName("requirementId")
    val requirementId: String? = null,

    @field:SerializedName("updatedAt")
    val updatedAt: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("checkOutBeforeSlot")
    val checkOutBeforeSlot: String? = null,

    @field:SerializedName("updatedBy")
    val updatedBy: String? = null,

    @field:SerializedName("agencyContact")
    val agencyContact: AgencyContact? = null,

    @field:SerializedName("checkOutAfterBuffer")
    val checkOutAfterBuffer: String,

    @field:SerializedName("businessContact")
    val businessContact: BusinessContact? = null,

    @field:SerializedName("isNewGig")
    val isNewGig: Boolean? = null,

    @field:SerializedName("profile")
    val profile: Profile? = null,

    @field:SerializedName("checkOutAfterSlot")
    val checkOutAfterSlot: String,

    @field:SerializedName("autoCheckout")
    val autoCheckout: String? = null,

    @field:SerializedName("checkInBeforeBuffer")
    val checkInBeforeBuffer: String,

    @field:SerializedName("endDateTime")
    val endDateTime: String,

    @field:SerializedName("checkOutBeforeBuffer")
    val checkOutBeforeBuffer: String,

    @field:SerializedName("giger")
    val giger: Giger? = null,

    @field:SerializedName("gigStatus")
    val gigStatus: String,

    @field:SerializedName("startDateTime")
    val startDateTime: String,

    @field:SerializedName("isFullDay")
    val isFullDay: Boolean? = null,

    @field:SerializedName("gigOrderId")
    val gigOrderId: String? = null,

    @field:SerializedName("checkInBeforeSlot")
    val checkInBeforeSlot: String,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("workOrderId")
    val workOrderId: String? = null,

    @field:SerializedName("attendance")
    val attendance: Attendance? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("latitude")
    var latitude: Double? = null,

    @field:SerializedName("longitude")
    var longitude: Double? = null,


    ) {

    fun toGigModel(): Gig {
        return Gig(
            tag = "",
            gigId = id ?: "",
            gigerId = gigerId ?: "",
            title = title ?: "",
            description = description ?: "",
            bannerImage = null,
            address = address ?: "",
            geoPoint = null,
            latitude = latitude,
            longitude = longitude,
            gigAmount = 0.0,
            invoiceGenerationDate = null,
            paymentStatus = "",
            cancellationReason = "",
            companyName = null,
            companyLogo = null,
            startDateTime = startDateTime.toTimestamp(),
            checkInBeforeTime = checkInBeforeBuffer.toTimestamp(),
            checkInBeforeBufferTime = checkInBeforeBuffer.toTimestamp(),
            checkInAfterBufferTime = checkInAfterBuffer.toTimestamp(),
            checkInAfterTime = checkInAfterSlot.toTimestamp(),
            endDateTime = endDateTime.toTimestamp(),
            checkOutBeforeTime = checkOutAfterSlot.toTimestamp(),
            checkOutBeforeBufferTime = checkOutBeforeBuffer.toTimestamp(),
            checkOutAfterBufferTime = checkOutAfterBuffer.toTimestamp(),
            checkOutAfterTime = checkOutAfterSlot.toTimestamp(),
            agencyContact = null,
            businessContact = null,
            assignedOn = assignOn.toTimestamp(),
            checkInBeforeTimeBufferInMins = 0,
            checkInAfterTimeBufferInMins = 0,
            checkOutBeforeTimeBufferInMins = 0,
            checkOutAfterTimeBufferInMins = 0,
            gigStatus = gigStatus,
            isGigActivated = false,
            isFavourite = false,
            isGigCompleted = false,
            isPaymentDone = false,
            isMonthlyGig = false,
            isFullDay = false,
            gigOrderId = "",
            profile = JobProfile(
                id = null,
                activationCode = null,
                title = null
            ),
            legalEntity = null,
            keywords = listOf(),
            gigRating = 0.0f,
            gigUserFeedback = null,
            gigUserFeedbackAttachments = listOf(),
            locationPictures = listOf(),
            ratingUserReceived = 0.0f,
            feedbackUserReceived = null,
            ratingUserReceivedAttachments = listOf(),
            gigType = null,
            gigHighlights = listOf(),
            gigRequirements = listOf(),
            gigResponsibilities = listOf(),
            attendance = if (attendance == null) {
                null
            } else {
                GigAttendance().apply {
                    this.checkInMarked = attendance.checkInMarked ?: false
                    this.checkInTime = attendance.checkInTime.toDate()
                    this.checkInLat = attendance.checkInLat ?: 0.0
                    this.checkInLong = attendance.checkInLong ?: 0.0
                    this.checkInImage = attendance.checkInImage ?: ""

                    this.checkOutMarked = attendance.checkOutMarked ?: false
                    this.checkOutAddress = attendance.checkOutAddress ?: ""
                    this.checkOutTime = attendance.checkOutTime.toDate()
                    this.checkOutLat = attendance.checkOutLat ?: 0.0
                    this.checkOutLong = attendance.checkOutLong ?: 0.0
                }
            },
            gigContactDetails = null,
            declinedBy = null,
            declineReason = null,
            payoutDetails = null,
            isNewGig = isNewGig,
            regularisationRequest = null,
            chatInfo = mapOf()
        )
    }

    private fun String.toTimestamp(): Timestamp {

        val localDateTime = LocalDateTime.parse(
            this,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )
        val dateFromServer =  localDateTime.toDate

        val newTime : Long = dateFromServer.time + 19800000L //5.5 Hour
        return Timestamp(Date(newTime))
    }

    private fun String?.toDate(): Date? {
        if (this == null) {
            return null
        } else {
            val localDateTime = LocalDateTime.parse(
                this,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            localDateTime.plusHours(5L)
            localDateTime.plusMinutes(30L)
            val dateFromServer =  localDateTime.toDate

            val newTime : Long = dateFromServer.time + 19800000L //5.5 Hour
            return Date(newTime)
        }
    }
}

data class LegalEntity(

    @field:SerializedName("tradingName")
    val tradingName: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("logo")
    val logo: String? = null,

    @field:SerializedName("id")
    val id: String? = null
)

data class Profile(

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("title")
    val title: String? = null
)

data class BusinessContact(

    @field:SerializedName("company_name")
    val companyName: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("designation")
    val designation: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("office")
    val office: Office? = null,

    @field:SerializedName("primary_no")
    val primaryNo: String? = null,

    @field:SerializedName("business_id")
    val businessId: String? = null,

    @field:SerializedName("createdOn")
    val createdOn: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class Office(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null
)

data class Attendance(

    @field:SerializedName("checkInSource")
    val checkInSource: String? = null,

    @field:SerializedName("checkInImage")
    val checkInImage: String? = null,

    @field:SerializedName("checkInLat")
    val checkInLat: Double? = null,

    @field:SerializedName("checkInLong")
    val checkInLong: Double? = null,

    @field:SerializedName("checkInLocationFake")
    val checkInLocationFake: Boolean? = null,

    @field:SerializedName("checkInTime")
    val checkInTime: String?,

    @field:SerializedName("checkInAddress")
    val checkInAddress: String? = null,

    @field:SerializedName("checkInGeoPoint")
    val checkInGeoPoint: CheckInGeoPoint? = null,

    @field:SerializedName("checkInLocationAccuracy")
    val checkInLocationAccuracy: Int? = null,

    @field:SerializedName("checkInMarked")
    val checkInMarked: Boolean? = null,

    @field:SerializedName("checkInDistanceBetweenGigAndUser")
    val checkInDistanceBetweenGigAndUser: Int? = null,


    @field:SerializedName("checkOutSource")
    val checkOutSource: String? = null,

    @field:SerializedName("checkOutImage")
    val checkOutImage: String? = null,

    @field:SerializedName("checkOutLat")
    val checkOutLat: Double? = null,

    @field:SerializedName("checkOutLong")
    val checkOutLong: Double? = null,

    @field:SerializedName("checkOutLocationFake")
    val checkOutLocationFake: Boolean? = null,

    @field:SerializedName("checkOutTime")
    val checkOutTime: String?,

    @field:SerializedName("checkOutAddress")
    val checkOutAddress: String? = null,

    @field:SerializedName("checkOutGeoPoint")
    val checkOutGeoPoint: CheckInGeoPoint? = null,

    @field:SerializedName("checkOutLocationAccuracy")
    val checkOutLocationAccuracy: Int? = null,

    @field:SerializedName("checkOutMarked")
    val checkOutMarked: Boolean? = null,

    @field:SerializedName("checkOutDistanceBetweenGigAndUser")
    val checkOutDistanceBetweenGigAndUser: Int? = null
)

data class Giger(

    @field:SerializedName("mobile")
    val mobile: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null
)

data class CheckInGeoPoint(

    @field:SerializedName("_longitude")
    val longitude: Double? = null,

    @field:SerializedName("_latitude")
    val latitude: Double? = null
)

data class AgencyContact(

    @field:SerializedName("company_name")
    val companyName: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("designation")
    val designation: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("office")
    val office: Office? = null,

    @field:SerializedName("primary_no")
    val primaryNo: String? = null,

    @field:SerializedName("business_id")
    val businessId: String? = null,

    @field:SerializedName("createdOn")
    val createdOn: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)
