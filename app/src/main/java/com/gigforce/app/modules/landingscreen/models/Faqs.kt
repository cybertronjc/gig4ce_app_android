package com.gigforce.app.modules.landingscreen.models

import android.os.Parcel
import android.os.Parcelable

data class Faqs(
        var answer: String = "",
        var longAnswer: String = "",
        var question: String = "",
        var htmlString: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(answer)
        parcel.writeString(longAnswer)
        parcel.writeString(question)
        parcel.writeString(htmlString)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Faqs> {
        override fun createFromParcel(parcel: Parcel): Faqs {
            return Faqs(parcel)
        }

        override fun newArray(size: Int): Array<Faqs?> {
            return arrayOfNulls(size)
        }
    }
}
