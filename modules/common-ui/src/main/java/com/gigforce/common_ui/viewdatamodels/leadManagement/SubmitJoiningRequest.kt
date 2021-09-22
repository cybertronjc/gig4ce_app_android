package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubmitJoiningRequest(

    @SerializedName("business")
    var business: JoiningBusinessAndJobProfilesItem,

    @SerializedName("jobProfile")
    var jobProfile: JobProfilesItem,

    @SerializedName("gigerClientId")
    var gigerClientId: String?,

    @SerializedName("gigerName")
    var gigerName: String,

    @SerializedName("gigerMobileNo")
    var gigerMobileNo: String,

    @SerializedName("assignGigsFrom")
    var assignGigsFrom: String = "",

    @SerializedName("businessTeamLeaders")
    var businessTeamLeaders: BusinessTeamLeadersItem = BusinessTeamLeadersItem(),

    @SerializedName("city")
    var city: ReportingLocationsItem = ReportingLocationsItem(),

    @SerializedName("reportingLocation")
    var reportingLocation: ReportingLocationsItem = ReportingLocationsItem(),

    @SerializedName("shifts")
    var shifts: List<ShiftTimingItem> = emptyList(),


) : Parcelable