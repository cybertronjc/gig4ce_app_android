package com.gigforce.common_ui.viewmodels.payouts

import com.google.gson.annotations.SerializedName

data class Payout(

	@field:SerializedName("paymentCycleEndDate")
	val paymentCycleEndDate: String? = null,

	@field:SerializedName("amount")
	val amount: String? = null,

	@field:SerializedName("businessIcon")
	val businessIcon: String? = null,

	@field:SerializedName("businessName")
	val businessName: String? = null,

	@field:SerializedName("businessid")
	val businessid: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("statusColorCode")
	val statusColorCode: String? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("paidOnDate")
	val paidOnDate: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
