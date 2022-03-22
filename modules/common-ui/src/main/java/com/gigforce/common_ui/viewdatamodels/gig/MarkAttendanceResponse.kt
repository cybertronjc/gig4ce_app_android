package com.gigforce.common_ui.viewdatamodels.gig

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.google.gson.annotations.SerializedName

data class MarkAttendanceResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean,

	@field:SerializedName("data")
	val data : GigAttendanceApiModel? = null
)
