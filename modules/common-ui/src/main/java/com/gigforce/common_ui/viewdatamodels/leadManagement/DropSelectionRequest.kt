package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DropSelectionRequest(

    @SerializedName("selectionsToDrop")
    var selectionsToDrop: List<DropDetail>? = null
): Parcelable {
}

@Parcelize
data class DropDetail (

    @SerializedName("joiningId")
    var joiningId: String? = null,

    @SerializedName("lastWorkingDate")
    var lastWorkingDate: String? = null,

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("droppedDate")
    var droppedDate: String? = null
): Parcelable