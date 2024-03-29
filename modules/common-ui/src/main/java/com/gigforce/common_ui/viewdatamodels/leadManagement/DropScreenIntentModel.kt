package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DropScreenIntentModel(
    var joiningId: String? = null,
    var gigId : String? ,
    var isBankVerified: Boolean = false,
    var hasStartEndDate: Boolean = false,
    var gigStartDate: String = "",
    var gigEndDate: String = "",
    var currentDate: String = ""
): Parcelable
