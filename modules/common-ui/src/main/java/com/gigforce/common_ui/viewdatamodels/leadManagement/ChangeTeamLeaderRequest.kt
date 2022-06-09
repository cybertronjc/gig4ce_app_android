package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class ChangeTeamLeaderRequest(

    @SerializedName("changeTlOfGigers")
    val changeTlOfGigers: List<ChangeTeamLeaderRequestItem>
)

@Parcelize
data class ChangeTeamLeaderRequestItem(

    @SerializedName("gigerUid")
    val gigerUid: String?,

    @SerializedName("gigerName")
    var gigerName: String?,

    @SerializedName("teamLeaderId")
    var teamLeaderId: String?,

    @SerializedName("joiningId")
    val joiningId: String? = null,

    @SerializedName("gigId")
    val gigId: String?,

    @SerializedName("jobProfileId")
    val jobProfileId: String?
) : Parcelable