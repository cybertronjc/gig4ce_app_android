package com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class DropGigerRequest(

	@field:SerializedName("reason")
	val reason: String? = null,

	@field:SerializedName("gigerId")
	val gigerId: String? = null,

	@field:SerializedName("lastWorkingDate")
	val lastWorkingDate: LocalDate? = null,

	@field:SerializedName("jobProfileId")
	val jobProfileId: String? = null
)
