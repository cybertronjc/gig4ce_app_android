package com.gigforce.modules.feature_chat.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SharedFile(
        val file : Uri,
        val text : String? = null
) : Parcelable