package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class RequirmentsGigActivation(var documentTitle: String = "", var requirementTitle: String = "", var testTitle: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentTitle)
        parcel.writeString(requirementTitle)
        parcel.writeString(testTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RequirmentsGigActivation> {
        override fun createFromParcel(parcel: Parcel): RequirmentsGigActivation {
            return RequirmentsGigActivation(parcel)
        }

        override fun newArray(size: Int): Array<RequirmentsGigActivation?> {
            return arrayOfNulls(size)
        }
    }
}