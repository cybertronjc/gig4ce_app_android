package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class GigApplication(
    @field:SerializedName("id")
    var id: String? = null,

    @field:SerializedName("gigerId")
    var gigerId: String? = null,

    @field:SerializedName("jpId")
    val jpId: String? = null,

    @field:SerializedName("profileId")
    val profileId: String? = null,

    @field:SerializedName("profileName")
    val profileName: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("jobProfileTitle")
    var jobProfileTitle: String? = null,

    var type: String? = null,
    var selected: Boolean = false

) {
}