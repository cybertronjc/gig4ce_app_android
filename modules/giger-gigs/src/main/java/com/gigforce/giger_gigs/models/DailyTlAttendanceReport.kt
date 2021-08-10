package com.gigforce.giger_gigs.models

import com.google.gson.annotations.SerializedName

data class DailyTlAttendanceReport(

	@field:SerializedName("UID")
	val uID: String? = null,

	@field:SerializedName("city")
	val city: City? = null,

	@field:SerializedName("businessData")
	val businessData: List<BusinessDataItem?>? = null,

	@field:SerializedName("update")
	val update: Boolean? = null,

	@field:SerializedName("id")
	val id: String? = null
)

data class City(

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("state_code")
	val stateCode: String? = null
)

data class BusinessDataItem(

	@field:SerializedName("newLoginToday")
	val newLoginToday: Int = 0,

	@field:SerializedName("openPositions")
	val openPositions: Int = 0,

	@field:SerializedName("expectedLoginsTomorrow")
	val expectedLoginsTomorrow: Int = 0,

	@field:SerializedName("absentToday")
	val absentToday: Int = 0,

	@field:SerializedName("city")
	val city: City? = null,

	@field:SerializedName("businessId")
	val businessId: String? = null,

	@field:SerializedName("businessName")
	val businessName: String? = null,

	@field:SerializedName("totalActive")
	val totalActive: Int = 0,

	@field:SerializedName("newOnboardingToday")
	val newOnboardingToday: Int = 0,

	@field:SerializedName("loginToday")
	val loginToday: Int = 0,

	@field:SerializedName("legalName")
	val legalName: String? = null,

	@field:SerializedName("resignedToday")
	val resignedToday: Int = 0,

	@field:SerializedName("inTrainingToday")
	val inTrainingToday: Int = 0,

	@field:SerializedName("totalLineupsForTomorrow")
	val totalLineupsForTomorrow: Int = 0
)
