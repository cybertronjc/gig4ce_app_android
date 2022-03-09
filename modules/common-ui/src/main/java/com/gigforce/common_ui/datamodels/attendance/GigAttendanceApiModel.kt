package com.gigforce.common_ui.datamodels.attendance

import com.google.gson.annotations.SerializedName

data class GigAttendanceApiModel(

	@field:SerializedName("attendanceType")
	val attendanceType: String? = null,

	@field:SerializedName("GigerName")
	val gigerName: String? = null,

	@field:SerializedName("BusinessName")
	val businessName: String? = null,

	@field:SerializedName("tlAttendance")
	val tlAttendance: TlAttendance? = null,

	@field:SerializedName("GigerMobile")
	val gigerMobile: String? = null,

	@field:SerializedName("gigerAttedance")
	val gigerAttedance: GigerAttedance? = null,

	@field:SerializedName("gigDate")
	val gigDate: String? = null,

	@field:SerializedName("finalAttendance")
	val finalAttendance: FinalAttendance? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("JobProfile")
	val jobProfile: String? = null,

	@field:SerializedName("BusinessId")
	val businessId: String? = null,

	@field:SerializedName("GigerId")
	val gigerId: String? = null
){
	fun getBusinessNameNN() : String{
		return businessName ?: "Others"
	}
}

data class FinalAttendance(

	@field:SerializedName("attendanceStatus")
	val attendanceStatus: String? = null
)

data class TlAttendance(

	@field:SerializedName("attendanceStatus")
	val attendanceStatus: String? = null
)

data class GigerAttedance(

	@field:SerializedName("attendanceStatus")
	val attendanceStatus: String? = null
)
