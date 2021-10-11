package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DropSelectionRequest(

    @SerializedName("selectionsToDrop")
    var selectionsToDrop: List<String>? = null
): Parcelable {
}