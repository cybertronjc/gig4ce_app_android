package com.gigforce.app.modules.questionnaire.models

import android.os.Parcel
import android.os.Parcelable

data class Questions(var question: String = "", var url: String = "", var options: List<Options> = listOf(), var selectedAnswer: Int = -1) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createTypedArrayList(Options) ?: listOf(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(question)
        parcel.writeString(url)
        parcel.writeTypedList(options)
        parcel.writeInt(-1)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Questions> {
        override fun createFromParcel(parcel: Parcel): Questions {
            return Questions(parcel)
        }

        override fun newArray(size: Int): Array<Questions?> {
            return arrayOfNulls(size)
        }
    }
}