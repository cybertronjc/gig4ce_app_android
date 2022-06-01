package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.gigforce.app.data.repositoriesImpl.gigs.GigerAttedance
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigAttendance
import com.gigforce.core.datamodels.gigpage.JobProfile
import com.gigforce.core.date.DateUtil
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class GigInfoBasicApiModel(

	@field:SerializedName("checkInAfterSlot")
	val checkInAfterSlot: Timestamp? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("checkInAfterBuffer")
	val checkInAfterBuffer: Timestamp? = null,

	@field:SerializedName("checkOutBeforeSlot")
	val checkOutBeforeSlot: Timestamp? = null,

	@field:SerializedName("gigerId")
	val gigerId: String? = null,

	@field:SerializedName("checkOutAfterBuffer")
	val checkOutAfterBuffer: Timestamp? = null,

	@field:SerializedName("checkOutAfterSlot")
	val checkOutAfterSlot: Timestamp? = null,

	@field:SerializedName("gigDate")
	val gigDate: Timestamp? = null,

	@field:SerializedName("checkInBeforeBuffer")
	val checkInBeforeBuffer: Timestamp? = null,

	@field:SerializedName("endDateTime")
	val endDateTime: Timestamp? = null,

	@field:SerializedName("checkOutBeforeBuffer")
	val checkOutBeforeBuffer: Timestamp? = null,

	@field:SerializedName("gigStatus")
	val gigStatus: String? = null,

	@field:SerializedName("startDateTime")
	val startDateTime: Timestamp? = null,

	@field:SerializedName("gigOrderId")
	val gigOrderId: String? = null,

	@field:SerializedName("checkInBeforeSlot")
	val checkInBeforeSlot: Timestamp? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("attendance")
	val attendance: GigerAttedance? = null,

	@field:SerializedName("legalEntity")
	val legalEntityApiModel: LegalEntityApiModel? = null,

	@field:SerializedName("profile")
	val jobProfile: Profile? = null,

	@field:SerializedName("agencyContact")
	val agencyContact: AgencyContact? = null
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
			startDateTime = startDateTime!!,
			checkInBeforeTime = checkInBeforeSlot!!,
			checkInBeforeBufferTime = checkInBeforeBuffer!!,
			checkInAfterBufferTime = checkInAfterBuffer!!,
			checkInAfterTime = checkInAfterSlot!!,
			endDateTime = endDateTime!!,
			checkOutBeforeTime = checkOutBeforeSlot!!,
			checkOutBeforeBufferTime = checkOutBeforeBuffer!!,
			checkOutAfterBufferTime = checkOutAfterBuffer!!,
			checkOutAfterTime = checkOutAfterSlot!!,
			agencyContact = agencyContact?.toContactPerson(),
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
			profile = jobProfile?.toJobProfilePresentationModel() ?: JobProfile(),
			legalEntity = legalEntityApiModel?.toPresentationLegalEnitityModel(),
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
					this.checkInTime = DateUtil.getDateFromUTCDateTimeStringOrNull(attendance.checkInTime)
					this.checkInLat =  0.0
					this.checkInLong =  0.0
					this.checkInImage = attendance.checkInImage ?: ""

					this.checkOutMarked = attendance.checkOutTime != null
					this.checkOutTime = DateUtil.getDateFromUTCDateTimeStringOrNull(attendance.checkOutTime)
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
}

