package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.google.gson.annotations.SerializedName

data class GigAttendanceRequest(

	@field:SerializedName("month")
	val month: Int,

	@field:SerializedName("year")
	val year: Int,

	@field:SerializedName("gigOrderId")
	val gigOrderId: String? = null
)
