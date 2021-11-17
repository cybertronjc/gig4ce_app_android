package com.gigforce.lead_management.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DropScreenIntentModel(
    var joiningId: String,
    var isBankVerified: Boolean = false,
    var gigStartDate: String = "",
    var gigEndDate: String = "",
    var currentDate: String = ""
): Parcelable
