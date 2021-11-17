package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.gson.annotations.SerializedName

data class GigerInfo(
    @SerializedName("gigerName")
    val gigerName: String,

    @SerializedName("gigerMobileNo")
    val gigerPhone: String?,

    @SerializedName("gigerId")
    val gigerId: String?,

    @SerializedName("gigerProfilePicture")
    val gigerProfilePicture: String?,

    @SerializedName("businessName")
    val businessName: String?,

    @SerializedName("businessLogo")
    val businessLogo: String,

    @SerializedName("jobProfileTitle")
    val jobProfileTitle: String,

    @SerializedName("joiningDate")
    val joiningDate: String,

    @SerializedName("businessLocation")
    val businessLocation: String?,

    @SerializedName("reportingLocation")
    val reportingLocation: String?,

    @SerializedName("selectionDate")
    val selectionDate: String,

    @SerializedName("gigStartDate")
    val gigStartDate: String,

    @SerializedName("gigEndDate")
    val gigEndDate: String,

    @SerializedName("currentDate")
    val currentDate: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("checkList")
    val checkList: List<CheckListItem>? = null,
) {
}

data class CheckListItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("optional")
    val optional: Boolean,

    @SerializedName("status")
    val status: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("frontImage")
    val frontImage: String? = "",

    @SerializedName("backImage")
    val backImage: String? = ""
){}