package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class ChangeTLResponse(

	@field:SerializedName("result")
	val result: List<ResultItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class Value(

	@field:SerializedName("gigerUid")
	val gigerUid: String? = null,

	@field:SerializedName("joiningId")
	val joiningId: String? = null,

	@field:SerializedName("gigerName")
	val gigerName: String? = null,

	@field:SerializedName("teamLeaderId")
	val teamLeaderId: String? = null
)

data class ResultItem(

	@field:SerializedName("errorMessage")
	val errorMessage: String? = null,

	@field:SerializedName("value")
	val value: Value? = null
)
