package com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info

import com.google.gson.annotations.SerializedName

data class GigerInfoApiModel(

	@field:SerializedName("clientId")
	val clientId: String? = null,

	@field:SerializedName("jobProfile")
	val jobProfile: String? = null,

	@field:SerializedName("lastActiveText")
	val lastActiveText: String? = null,

	@field:SerializedName("gigerId")
	val gigerId: String? = null,

	@field:SerializedName("businessId")
	val businessId: String? = null,

	@field:SerializedName("businessName")
	val businessName: String? = null,

	@field:SerializedName("pendingComplianceInformation")
	val pendingComplianceInformation: PendingComplianceInformation? = null,

	@field:SerializedName("currentDate")
	val currentDate: String? = null,

	@field:SerializedName("joiningDate")
	val joiningDate: String? = null,

	@field:SerializedName("gigerMobile")
	val gigerMobile: String? = null,

	@field:SerializedName("gigerProfilePicture")
	val gigerProfilePicture: String? = null,

	@field:SerializedName("payoutInformation")
	val payoutInformation: PayoutInformation? = null,

	@field:SerializedName("gigerProfilePictureThumbnail")
	val gigerProfilePictureThumbnail: String? = null,

	@field:SerializedName("gigerName")
	val gigerName: String? = null,

	@field:SerializedName("businessIcon")
	val businessIcon: String? = null,

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("jobProfileId")
	val jobProfileId: String? = null,

	@field:SerializedName("scout")
	val scout: Scout? = null,

	@field:SerializedName("retention")
	val retention: Retention? = null
)

data class PendingComplianceInformation(

	@field:SerializedName("string")
	val string: String? = null,

	@field:SerializedName("backgroundColorCode")
	val backgroundColorCode: String? = null,

	@field:SerializedName("pririty")
	val pririty: String? = null
)

data class Retention(

	@field:SerializedName("lastActiveWarningString")
	val lastActiveWarningString: String? = null
)

data class PayoutInformation(

	@field:SerializedName("amount")
	val amount: Int? = null,

	@field:SerializedName("paymentCycleEndDate")
	val paymentCycleEndDate: Any? = null,

	@field:SerializedName("pdfUrl")
	val pdfUrl: String? = null,

	@field:SerializedName("payoutStatus")
	val payoutStatus: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("payOutCycle")
	val payOutCycle: Any? = null,

	@field:SerializedName("statusColorCode")
	val statusColorCode: String? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("paidOnDate")
	val paidOnDate: Any? = null,

	@field:SerializedName("status")
	val status: List<String?>? = null,

	@field:SerializedName("colorString")
	val colorString: String? = null
)

data class Scout(

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("id")
	val id: String? = null
)
