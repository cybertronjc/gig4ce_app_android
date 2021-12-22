package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class JoiningBusinessJobProfilesAndTeamsLeaders(

    @field:SerializedName("businessAndJobProfiles")
    val businessAndJobProfiles: List<JoiningBusinessAndJobProfilesItem> = emptyList(),

    @field:SerializedName("teamLeaders")
    val teamLeaders: List<TeamLeader> = emptyList(),
)


@Parcelize
data class JoiningBusinessAndJobProfilesItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("icon")
    val icon: String? = null,

    @field:SerializedName("jobProfiles")
    var jobProfiles: List<JobProfilesItem>,

    @DoNotSerialize
    var selected: Boolean = false

) : Parcelable

@Parcelize
data class JobProfilesItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("locationType")
    val locationType: String? = null,

    @field:SerializedName("dynamicVerificationInputFields")
    var verificationRelatedFields: List<DynamicVerificationField> = emptyList(),

    @field:SerializedName("dynamicInputFields")
    var dynamicFields: List<DynamicField> = emptyList(),

    @DoNotSerialize
    var selected: Boolean = false
) : Parcelable



@Parcelize
data class TeamLeader(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("designation")
    val designation: String? = null,

    @field:SerializedName("cityId")
    val cityId: String? = null,

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("profilePictureThumbnail")
    val profilePictureThumbnail: String? = null,

    @field:SerializedName("profilePicture")
    val profilePicture: String? = null,

    @DoNotSerialize
    var selected: Boolean = false,
) : Parcelable{

    fun isTeamLeaderEqual(
        teamLeaderUid : String
    ) : Boolean = teamLeaderUid == id

}


