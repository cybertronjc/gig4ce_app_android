package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class Locations(var location: String? = null, var payoutNote: String? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(location)
        parcel.writeString(payoutNote)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Locations> {
        override fun createFromParcel(parcel: Parcel): Locations {
            return Locations(parcel)
        }

        override fun newArray(size: Int): Array<Locations?> {
            return arrayOfNulls(size)
        }
    }
}