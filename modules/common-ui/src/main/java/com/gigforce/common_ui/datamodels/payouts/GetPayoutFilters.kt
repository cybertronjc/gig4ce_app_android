package com.gigforce.common_ui.datamodels.payouts

import com.google.gson.annotations.SerializedName

data class GetPayoutFilters(

	@field:SerializedName("filters")
	val filters: Filters? = null
)

data class Filters(

	@field:SerializedName("endDate")
	val endDate: String? = null,

	@field:SerializedName("startDate")
	val startDate: String? = null
)
