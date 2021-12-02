package com.gigforce.lead_management.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhatsappTemplateModel(
    val shareLink: String,
    val businessName: String,
    val tlName: String,
    val jobProfileName: String,
    val tlMobileNumber: String
): Parcelable {
}