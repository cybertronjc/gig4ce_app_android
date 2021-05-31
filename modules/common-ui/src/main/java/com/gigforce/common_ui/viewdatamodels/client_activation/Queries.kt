package com.gigforce.common_ui.viewdatamodels.client_activation

import android.os.Parcel
import android.os.Parcelable

data class Queries(var answer: String? = null, var icon: String? = null, var query: String? = null):Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(answer)
        parcel.writeString(icon)
        parcel.writeString(query)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Queries> {
        override fun createFromParcel(parcel: Parcel): Queries {
            return Queries(parcel)
        }

        override fun newArray(size: Int): Array<Queries?> {
            return arrayOfNulls(size)
        }
    }

}