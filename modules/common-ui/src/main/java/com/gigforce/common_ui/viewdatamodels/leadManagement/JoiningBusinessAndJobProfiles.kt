package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class JoiningBusinessAndJobProfilesItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("icon")
	val icon: String? = null,

	@field:SerializedName("jobProfiles")
	val jobProfiles: List<JobProfilesItem>,

	@DoNotSerialize
	var selected : Boolean = false
) : Parcelable

@Parcelize
data class JobProfilesItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@DoNotSerialize
	var selected : Boolean = false
) : Parcelable
