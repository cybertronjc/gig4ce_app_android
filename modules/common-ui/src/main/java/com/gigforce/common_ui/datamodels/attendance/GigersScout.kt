package com.gigforce.common_ui.datamodels.attendance

import com.google.gson.annotations.SerializedName

data class GigersScout(

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("id")
	val id: String? = null
)
