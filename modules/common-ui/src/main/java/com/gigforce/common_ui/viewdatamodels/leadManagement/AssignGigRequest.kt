package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssignGigRequest(

    @SerializedName("joiningId")
    var joiningId: String = "",

    @SerializedName("jobProfileId")
    var jobProfileId: String = "",

    @SerializedName("jobProfileName")
    var jobProfileName: String = "",

    @SerializedName("companyLogo")
    var companyLogo: String = "",

    @SerializedName("userName")
    var userName: String = "",

    @SerializedName("userUid")
    var userUid: String = "",

    @SerializedName("enrollingTlUid")
    var enrollingTlUid: String = "",

    @SerializedName("assignGigsFrom")
    var assignGigsFrom: Timestamp = Timestamp.now(),

    @SerializedName("cityId")
    var cityId: String = "",

    @SerializedName("cityName")
    var cityName: String = "",

    @SerializedName("location")
    var location : JobLocation? = JobLocation(),

    @SerializedName("shift")
    var shift : List<JobShift>? = emptyList(),

    @SerializedName("gigForceTeamLeaders")
    var gigForceTeamLeaders : List<JobTeamLeader>? = emptyList(),

    @SerializedName("businessTeamLeaders")
    var businessTeamLeaders : List<JobTeamLeader>? = emptyList()
) : Parcelable
