package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class RequiredLessons(var icon: String? = null, var title: String? = null, var lessons: List<String>? = null):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(icon)
        parcel.writeString(title)
        parcel.writeStringList(lessons)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RequiredLessons> {
        override fun createFromParcel(parcel: Parcel): RequiredLessons {
            return RequiredLessons(parcel)
        }

        override fun newArray(size: Int): Array<RequiredLessons?> {
            return arrayOfNulls(size)
        }
    }
}