package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import com.google.gson.annotations.SerializedName

data class MarkAttendanceResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean,

	@field:SerializedName("data")
	val data : GigAttendanceApiModel? = null
)
