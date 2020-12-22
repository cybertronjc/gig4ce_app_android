package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class PartnerSchoolDetails(
    var line2: String? = null, var landmark: String? = "", var name:
    String? = null, var line3: String? = null, var contact: List<ContactPartnerSchool> = listOf(),
    var lat: String = "", var lon: String = "",
    var line1: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(ContactPartnerSchool) ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(line2)
        parcel.writeString(landmark)
        parcel.writeString(name)
        parcel.writeString(line3)
        parcel.writeTypedList(contact)
        parcel.writeString(lat)
        parcel.writeString(lon)
        parcel.writeString(line1)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PartnerSchoolDetails> {
        override fun createFromParcel(parcel: Parcel): PartnerSchoolDetails {
            return PartnerSchoolDetails(parcel)
        }

        override fun newArray(size: Int): Array<PartnerSchoolDetails?> {
            return arrayOfNulls(size)
        }
    }
}