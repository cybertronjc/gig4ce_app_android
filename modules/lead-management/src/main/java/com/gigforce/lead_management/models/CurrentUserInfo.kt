package com.gigforce.lead_management.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrentUserInfo(
    val userName: String,
    val userProfilePicture: String,
    val userProfilePictureThumbnail: String,
    val userMobileNo: String,
    val jobProfileName: String,
    val jobProfileIcon: String
) : Parcelable
