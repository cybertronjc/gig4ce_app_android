package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.gigforce.core.retrofit.DoNotSerialize
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TeamLeaderSubmitModel(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("designation")
    val designation: String? = null,

    @field:SerializedName("mobile")
    val mobileNumber: String? = null,

    @DoNotSerialize
    var selected: Boolean = false,
) : Parcelable