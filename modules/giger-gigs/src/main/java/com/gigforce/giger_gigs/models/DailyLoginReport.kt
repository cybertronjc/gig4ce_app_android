package com.gigforce.giger_gigs.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DailyLoginReport(

	@field:SerializedName("dataTotal")
	val dataTotal: DataTotal? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("UID")
	val uID: String? = null,

	@field:SerializedName("businessData")
	val businessData: BusinessData? = null,

	@field:SerializedName("city")
	val city: LoginSummaryCity? = null,

	@field:SerializedName("totalPages")
	val totalPages: Int? = null,

	@field:SerializedName("_id")
	val _id: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("dateTimestamp")
	val dateTimestamp: Long
) : Parcelable

@Parcelize
data class DataTotal(

	@field:SerializedName("resignedToday")
	val resignedToday: Int? = null,

	@field:SerializedName("inTrainingToday")
	val inTrainingToday: Int? = null,

	@field:SerializedName("expectedLoginsTomorrow")
	val expectedLoginsTomorrow: Int? = null,

	@field:SerializedName("newLoginToday")
	val newLoginToday: Int? = null,

	@field:SerializedName("openPositions")
	val openPositions: Int? = null,

	@field:SerializedName("absentToday")
	val absentToday: Int? = null,

	@field:SerializedName("totalActive")
	val totalActive: Int? = null,

	@field:SerializedName("newOnboardingToday")
	val newOnboardingToday: Int? = null,

	@field:SerializedName("businessCount")
	val businessCount: Int? = null,

	@field:SerializedName("loginToday")
	val loginToday: Int? = null,

	@field:SerializedName("totalLineupsForTomorrow")
	val totalLineupsForTomorrow: Int? = null
): Parcelable

@Parcelize
data class BusinessData(

	@field:SerializedName("expectedLoginsTomorrow")
	val expectedLoginsTomorrow: Int? = null,

	@field:SerializedName("newLoginToday")
	val newLoginToday: Int? = null,

	@field:SerializedName("openPositions")
	val openPositions: Int? = null,

	@field:SerializedName("absentToday")
	val absentToday: Int? = null,

	@field:SerializedName("city")
	val city: LoginSummaryCity? = null,

	@field:SerializedName("businessId")
	val businessId: String? = null,

	@field:SerializedName("businessName")
	val businessName: String? = null,

	@field:SerializedName("totalActive")
	val totalActive: Int? = null,

	@field:SerializedName("newOnboardingToday")
	val newOnboardingToday: Int? = null,

	@field:SerializedName("loginToday")
	val loginToday: Int? = null,

	@field:SerializedName("legalName")
	val legalName: String? = null,

	@field:SerializedName("resignedToday")
	val resignedToday: Int? = null,

	@field:SerializedName("inTrainingToday")
	val inTrainingToday: Int? = null,

	@field:SerializedName("jobProfileName")
	val jobProfileName: String? = null,

	@field:SerializedName("jobProfileId")
	val jobProfileId: String? = null,

	@field:SerializedName("totalLineupsForTomorrow")
	val totalLineupsForTomorrow: Int? = null
): Parcelable

@Parcelize
data class City(

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("state_code")
	val stateCode: String? = null
): Parcelable
