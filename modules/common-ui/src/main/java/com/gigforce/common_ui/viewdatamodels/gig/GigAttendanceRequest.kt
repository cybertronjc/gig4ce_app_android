package com.gigforce.common_ui.viewdatamodels.gig

import com.google.gson.annotations.SerializedName

data class GigAttendanceRequest(

	@field:SerializedName("month")
	val month: Int,

	@field:SerializedName("year")
	val year: Int,

	@field:SerializedName("gigOrderId")
	val gigOrderId: String? = null
)
