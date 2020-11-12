package com.gigforce.app.modules.gigPage.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Keep
data class GigContactDetails(
    var contactName: String? = null,
    var contactNumber: Long = 0
)