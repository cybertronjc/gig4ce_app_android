package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class PartnerSchool(var workOrderId: String = "", var type: String = "", var schoolAddress: List<PartnerSchoolDetails> = listOf()) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createTypedArrayList(PartnerSchoolDetails) ?: listOf()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(workOrderId)
        parcel.writeString(type)
        parcel.writeTypedList(schoolAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PartnerSchool> {
        override fun createFromParcel(parcel: Parcel): PartnerSchool {
            return PartnerSchool(parcel)
        }

        override fun newArray(size: Int): Array<PartnerSchool?> {
            return arrayOfNulls(size)
        }
    }
}