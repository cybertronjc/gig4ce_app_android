package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicScreenField
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubmitJoiningRequest(

    @SerializedName("business")
    var business: JoiningBusinessAndJobProfilesItem,

    @SerializedName("jobProfile")
    var jobProfile: JobProfilesItem,

    @SerializedName("gigerName")
    var gigerName: String,

    @SerializedName("gigerMobileNo")
    var gigerMobileNo: String,

    @SerializedName("dataFromDynamicFields")
    var dataFromDynamicFields : List<DataFromDynamicInputField>,

    @SerializedName("dataFromDynamicScreenFields")
    var dataFromDynamicScreenFields : List<DataFromDynamicScreenField>? = null,

    @SerializedName("assignGigsFrom")
    var assignGigsFrom: String = "",

    @SerializedName("reportingTeamLeader")
    var reportingTeamLeader: TeamLeaderSubmitModel,

    @SerializedName("businessTeamLeaders")
    var businessTeamLeaders: BusinessTeamLeadersItem? = null,

    @SerializedName("city")
    var city: ReportingLocationsItem = ReportingLocationsItem(),

    @SerializedName("reportingLocation")
    var reportingLocation: ReportingLocationsItem? = null,

    @SerializedName("shifts")
    var shifts: List<ShiftTimingItem> = emptyList(),

    @SerializedName("shareLink")
    var shareLink: String = "",

    @SerializedName("secondaryMobileNumber")
    var secondaryMobileNumber: String? = null,
) : Parcelable