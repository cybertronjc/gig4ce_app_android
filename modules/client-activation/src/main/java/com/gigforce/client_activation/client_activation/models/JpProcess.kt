package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class JpProcess(@JvmField var isDone: Boolean = false, var title: String = "", var type: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isDone) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JpDraft> {
        override fun createFromParcel(parcel: Parcel): JpDraft {
            return JpDraft(parcel)
        }

        override fun newArray(size: Int): Array<JpDraft?> {
            return arrayOfNulls(size)
        }
    }
}