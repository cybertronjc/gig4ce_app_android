package com.gigforce.common_ui.viewdatamodels.gig

import com.gigforce.common_ui.datamodels.attendance.GigerAttedance
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigAttendance
import com.gigforce.core.datamodels.gigpage.JobProfile
import com.gigforce.core.extensions.toDate
import com.gigforce.core.extensions.toFirebaseTimestampOrNull
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class GigInfoBasicApiModel(

	@field:SerializedName("checkInAfterSlot")
	val checkInAfterSlot: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("checkInAfterBuffer")
	val checkInAfterBuffer: String? = null,

	@field:SerializedName("checkOutBeforeSlot")
	val checkOutBeforeSlot: String? = null,

	@field:SerializedName("gigerId")
	val gigerId: String? = null,

	@field:SerializedName("checkOutAfterBuffer")
	val checkOutAfterBuffer: String? = null,

	@field:SerializedName("checkOutAfterSlot")
	val checkOutAfterSlot: String? = null,

	@field:SerializedName("gigDate")
	val gigDate: String? = null,

	@field:SerializedName("checkInBeforeBuffer")
	val checkInBeforeBuffer: String? = null,

	@field:SerializedName("endDateTime")
	val endDateTime: String? = null,

	@field:SerializedName("checkOutBeforeBuffer")
	val checkOutBeforeBuffer: String? = null,

	@field:SerializedName("gigStatus")
	val gigStatus: String? = null,

	@field:SerializedName("startDateTime")
	val startDateTime: String? = null,

	@field:SerializedName("gigOrderId")
	val gigOrderId: String? = null,

	@field:SerializedName("checkInBeforeSlot")
	val checkInBeforeSlot: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("attendance")
	val attendance: GigerAttedance? = null
){


	fun toGig() : Gig{
		return Gig(
			tag = "",
			gigId = id ?: "",
			gigerId = gigerId ?: "",
			title = designation ?: "",
			description =  "",
			bannerImage = null,
			address = address ?: "",
			geoPoint = null,
			latitude = 0.0,
			longitude = 0.0,
			gigAmount = 0.0,
			invoiceGenerationDate = null,
			paymentStatus = "",
			cancellationReason = "",
			companyName = null,
			companyLogo = null,
			startDateTime = startDateTime.toFirebaseTimestampOrNull()!!,
			checkInBeforeTime = checkInBeforeBuffer.toFirebaseTimestampOrNull()!!,
			checkInBeforeBufferTime = checkInBeforeBuffer.toFirebaseTimestampOrNull()!!,
			checkInAfterBufferTime = checkInAfterBuffer.toFirebaseTimestampOrNull()!!,
			checkInAfterTime = checkInAfterSlot.toFirebaseTimestampOrNull()!!,
			endDateTime = endDateTime.toFirebaseTimestampOrNull()!!,
			checkOutBeforeTime = checkOutAfterSlot.toFirebaseTimestampOrNull()!!,
			checkOutBeforeBufferTime = checkOutBeforeBuffer.toFirebaseTimestampOrNull()!!,
			checkOutAfterBufferTime = checkOutAfterBuffer.toFirebaseTimestampOrNull()!!,
			checkOutAfterTime = checkOutAfterSlot.toFirebaseTimestampOrNull()!!,
			agencyContact = null,
			businessContact = null,
			assignedOn = Timestamp.now(),
			checkInBeforeTimeBufferInMins = 0,
			checkInAfterTimeBufferInMins = 0,
			checkOutBeforeTimeBufferInMins = 0,
			checkOutAfterTimeBufferInMins = 0,
			gigStatus = gigStatus!!,
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
					this.checkInMarked = attendance.checkInTime != null
					this.checkInTime = attendance.checkInTime.toDate()
					this.checkInLat =  0.0
					this.checkInLong =  0.0
					this.checkInImage = attendance.checkInImage ?: ""

					this.checkOutMarked = attendance.checkOutTime != null
					this.checkOutTime = attendance.checkOutTime.toDate()
					this.checkOutLat =  0.0
					this.checkOutLong =  0.0
				}
			},
			gigContactDetails = null,
			declinedBy = null,
			declineReason = null,
			payoutDetails = null,
			isNewGig = true,
			regularisationRequest = null,
			chatInfo = mapOf()
		)
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

