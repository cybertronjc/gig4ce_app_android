package com.gigforce.common_ui.datamodels.attendance

import com.google.gson.annotations.SerializedName

data class GigLocation(

	@field:SerializedName("stateId")
	val stateId: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("cityId")
	val cityId: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null
)
