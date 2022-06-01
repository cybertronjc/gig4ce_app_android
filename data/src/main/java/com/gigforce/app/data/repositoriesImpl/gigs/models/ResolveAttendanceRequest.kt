package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.google.gson.annotations.SerializedName

object ResolveAttendanceRequestOptions{

	const val YES = "Yes"
	const val NO = "No"

	fun fromBoolean(
		option : Boolean
	) : String{

		if(option)
			return YES
		else
			return NO
	}
}

data class ResolveAttendanceRequest(

	/**
	 * Option can be
	 * Yes
	 * No
	 */
	@field:SerializedName("response")
	val optionSelected: String,

	@field:SerializedName("resolveId")
	val resolveId: String
)
