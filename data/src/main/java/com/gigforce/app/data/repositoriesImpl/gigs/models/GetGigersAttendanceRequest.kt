package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class GetGigersAttendanceRequest(

	@field:SerializedName("filter")
	val filter: GetGigersAttendanceRequestFilter
)

data class GetGigersAttendanceRequestFilter(

	@field:SerializedName("date")
	val date: LocalDate
)
