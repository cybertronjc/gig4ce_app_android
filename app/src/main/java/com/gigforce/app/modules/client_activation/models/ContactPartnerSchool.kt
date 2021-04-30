package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class ContactPartnerSchool(var name: String = "", var number: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString()?:"") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContactPartnerSchool> {
        override fun createFromParcel(parcel: Parcel): ContactPartnerSchool {
            return ContactPartnerSchool(parcel)
        }

        override fun newArray(size: Int): Array<ContactPartnerSchool?> {
            return arrayOfNulls(size)
        }
    }
}