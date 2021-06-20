package com.gigforce.common_ui.viewdatamodels.gig

import com.google.gson.annotations.SerializedName

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



)
