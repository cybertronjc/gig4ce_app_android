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

    @SerializedName("shifts")
    val shifts: List<JobShift>,

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

    @SerializedName("jobLocations")
    val jobLocations: List<JobLocation>,

    @SerializedName("shifts")
    val shifts: List<JobShift>,
) : Parcelable

@Parcelize
data class JobLocation(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("name")
    val name: String?
) : Parcelable

@Parcelize
data class JobShift(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?
) : Parcelable

@Parcelize
data class JobTeamLeader(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?
) : Parcelable
