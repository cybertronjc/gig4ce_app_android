package com.gigforce.common_ui.viewdatamodels.client_activation

import android.os.Parcel
import android.os.Parcelable

data class RequiredMedia(var icon: String = "", var title: String = "", var media: List<Media> = listOf()) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createTypedArrayList(Media) ?: listOf()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(icon)
        parcel.writeString(title)
        parcel.writeTypedList(media)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RequiredMedia> {
        override fun createFromParcel(parcel: Parcel): RequiredMedia {
            return RequiredMedia(parcel)
        }

        override fun newArray(size: Int): Array<RequiredMedia?> {
            return arrayOfNulls(size)
        }
    }
}