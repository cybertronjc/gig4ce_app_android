package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.app.modules.landingscreen.models.Dependency

data class JpSettings(
        var requiredFeatures: List<Dependency>? = null,
        var coverImg: String? = null,
        var subTitle: String? = null,
        var title: String? = null,
        var type: String? = null,
        var workOrderId: String? = null,
        var nextDependency: String = "",
        var jobProfileId: String = "",
        var completionMessage: String = "",
        var completionTitle: String = "",
        var completionImage: String = "",
        var step: Int = 2
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createTypedArrayList(Dependency),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(requiredFeatures)
        parcel.writeString(coverImg)
        parcel.writeString(subTitle)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeString(workOrderId)
        parcel.writeString(nextDependency)
        parcel.writeString(jobProfileId)
        parcel.writeString(completionMessage)
        parcel.writeString(completionTitle)
        parcel.writeString(completionImage)
        parcel.writeInt(step)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JpSettings> {
        override fun createFromParcel(parcel: Parcel): JpSettings {
            return JpSettings(parcel)
        }

        override fun newArray(size: Int): Array<JpSettings?> {
            return arrayOfNulls(size)
        }
    }
}