package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class AddressHeader(var title: String = "", var subTitle: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressHeader> {
        override fun createFromParcel(parcel: Parcel): AddressHeader {
            return AddressHeader(parcel)
        }

        override fun newArray(size: Int): Array<AddressHeader?> {
            return arrayOfNulls(size)
        }
    }
}