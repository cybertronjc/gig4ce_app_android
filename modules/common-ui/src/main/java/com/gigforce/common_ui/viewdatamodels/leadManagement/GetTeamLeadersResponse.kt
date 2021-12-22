package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class GetTeamLeadersResponse(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class Result(

	@field:SerializedName("teamLeaders")
	val teamLeaders: List<TeamLeader> = emptyList()
)

@Parcelize
data class TeamLeader(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("cityId")
	val cityId: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("profilePictureThumbnail")
	val profilePictureThumbnail: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@DoNotSerialize
	var selected: Boolean = false,
) : Parcelable {

	fun isTeamLeaderEqual(
		teamLeaderUid : String
	) : Boolean = teamLeaderUid == id

}
