package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JobProfileDetails(

    @SerializedName("jobProfileId")
    val jobProfileId: String,

    @SerializedName("cityAndLocations")
    val cityAndLocations: List<JobProfileCityAndLocation>,

    @SerializedName("locality")
    val locality: List<JobLocality>,

    @SerializedName("stores")
    val stores: List<JobStore>,

    @SerializedName("shifts")
    val shifts: List<JobShift>,

    @SerializedName("workingDays")
    val workingDays: List<WorkingDays>,

    @SerializedName("gigforceTeamLeaders")
    val gigforceTeamLeaders: List<JobTeamLeader>,

    @SerializedName("businessTeamLeaders")
    val businessTeamLeaders: List<JobTeamLeader>
) : Parcelable

@Parcelize
data class JobProfileCityAndLocation(
    @SerializedName("id")
    val id: String,

    @SerializedName("city")
    val city: String?,
) : Parcelable

@Parcelize
data class JobLocation(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("name")
    val name: String? = ""
) : Parcelable

@Parcelize
data class JobStore(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("cityId")
    val cityId: String? = ""
) : Parcelable

@Parcelize
data class JobLocality(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("cityId")
    val cityId: String? = ""
) : Parcelable

@Parcelize
data class JobShift(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?,

) : Parcelable

@Parcelize
data class JobTeamLeader(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("primary")
    var primary : Boolean = false
) : Parcelable

@Parcelize
data class WorkingDays(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String?
) : Parcelable