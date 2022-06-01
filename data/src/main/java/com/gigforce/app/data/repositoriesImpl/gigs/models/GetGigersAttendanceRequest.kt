package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import com.gigforce.app.domain.models.tl_workspace.retention.StatusMasterWithCountItem
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


data class GetGigersAttendanceResponse(

	@field:SerializedName("gigers")
	val gigers: List<GigAttendanceApiModel>,

	@field:SerializedName("statusCount")
	val statusCount: List<StatusMasterWithCountItem>,
)
