package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
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






