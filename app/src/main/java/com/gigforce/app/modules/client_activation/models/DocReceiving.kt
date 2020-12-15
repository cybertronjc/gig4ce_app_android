package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class DocReceiving(
    var title: String = "",
    var subtitle: String = "",
    var checkItems: List<CheckItem> = listOf(),
    var jobProfileId: String = "",
    var type: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(CheckItem) ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeTypedList(checkItems)
        parcel.writeString(jobProfileId)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DocReceiving> {
        override fun createFromParcel(parcel: Parcel): DocReceiving {
            return DocReceiving(parcel)
        }

        override fun newArray(size: Int): Array<DocReceiving?> {
            return arrayOfNulls(size)
        }
    }
}