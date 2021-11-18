package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DropSelectionResponse(

    @SerializedName("status")
    var status: Boolean = false,

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("misingFields")
    var misingFields: List<DropMissingField>? = null
): Parcelable {
}

@Parcelize
data class DropMissingField(
    @SerializedName("joiningId")
    var joiningId: String = "",

    @SerializedName("lastWorkingDate")
    var lastWorkingDate: String? = null,

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("errorMessage")
    var errorMessage: String? = null
): Parcelable {
}
