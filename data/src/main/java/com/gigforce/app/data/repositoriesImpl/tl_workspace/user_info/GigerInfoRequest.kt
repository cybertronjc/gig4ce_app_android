package com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info

import com.google.gson.annotations.SerializedName

data class GigerInfoRequest(

	@field:SerializedName("requiredData")
	val requiredData: String,

    @field:SerializedName("gigerId")
    val gigerId: String,

    @field:SerializedName("businessId")
    val businessId: String,

    @field:SerializedName("jobProfileId")
    val jobProfileId: String,

	@field:SerializedName("payoutId")
	val payoutId: String? = null,

	@field:SerializedName("eJoiningId")
	val eJoiningId: String? = null,
)
