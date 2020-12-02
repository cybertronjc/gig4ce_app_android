package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class PartnerSchoolDetails(
    var city: String = "", var landmark: String = "", var name:
    String = "", var timing: String = "", var contact: List<ContactPartnerSchool> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(ContactPartnerSchool) ?: listOf()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(city)
        parcel.writeString(landmark)
        parcel.writeString(name)
        parcel.writeString(timing)
        parcel.writeTypedList(contact)
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