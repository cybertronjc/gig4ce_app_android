package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("subLocationFound")
	val subLocationFound: Boolean? = null,

	@field:SerializedName("is_active")
	val isActive: Boolean? = null,

	@field:SerializedName("cityCode")
	val cityCode: String? = null,

	@field:SerializedName("icon")
	val icon: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("state_code")
	val stateCode: String? = null,

	@field:SerializedName("majorCity")
	val majorCity: Boolean? = null
) : Parcelable
