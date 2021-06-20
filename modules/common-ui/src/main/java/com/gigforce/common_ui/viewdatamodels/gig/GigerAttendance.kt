package com.gigforce.common_ui.viewdatamodels.gig

import com.gigforce.core.extensions.toLocalDateTime
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

data class GigerAttendance(

	@field:SerializedName("shiftTime")
	val shiftTime: String? = null,

	@field:SerializedName("uid")
	val uid: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("companyName")
	val companyName: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("phonenumber")
	val phoneNumber: String? = null,

	@field:SerializedName("profilepicture")
	val profilePicture: String? = null,

	@field:SerializedName("attendanceStatus")
	var attendanceStatus: String? = null,

	@field:SerializedName("status")
	var gigStatus: String? = null,

	@field:SerializedName("gigId")
	val gigId: String? = null,

	@field:SerializedName("location")
	val location: String? = null
){

	private var timeFormat24Hour: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
	private var timeFormat12Hour: SimpleDateFormat =
			SimpleDateFormat("hh:mm a", Locale.getDefault())

	fun getShiftStartTime() : LocalDateTime{
		if (shiftTime.isNullOrBlank()) LocalDateTime.now()
		if (!shiftTime!!.contains("-")) return LocalDateTime.now()

		val shiftTimes = shiftTime.split("-")
		val currentTime = LocalDateTime.now()

		val time1 = timeFormat24Hour.parse(shiftTimes[0].trim())
		return time1.toLocalDateTime()
	}

	fun getShiftEndTime() : LocalDateTime{
		if (shiftTime.isNullOrBlank()) LocalDateTime.now()
		if (!shiftTime!!.contains("-")) return LocalDateTime.now()

		val shiftTimes = shiftTime.split("-")
		val currentTime = LocalDateTime.now()

		val time2 = timeFormat24Hour.parse(shiftTimes[1].trim())
		return time2.toLocalDateTime()
	}

}
