package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class JoiningLocationTeamLeadersShifts(

	@field:SerializedName("reportingLocations")
	val reportingLocations: List<ReportingLocationsItem>,

	@field:SerializedName("businessTeamLeaders")
	val businessTeamLeaders: List<BusinessTeamLeadersItem>,

	@field:SerializedName("shiftTiming")
	val shiftTiming: List<ShiftTimingItem>
)

@Parcelize
data class ReportingLocationsItem(

	@field:SerializedName("stateId")
	val stateId: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("cityId")
	val cityId: String? = null,

	@field:SerializedName("type")
	val type: String? = null
) : Parcelable

@Parcelize
data class BusinessTeamLeadersItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null
) : Parcelable

@Parcelize
data class ShiftTimingItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null
) : Parcelable
