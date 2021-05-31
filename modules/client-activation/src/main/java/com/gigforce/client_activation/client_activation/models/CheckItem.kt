package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class CheckItem(
    var content: String = "", @JvmField var isForKitCollection: Boolean = false,
    @get:Exclude var isCheckedBoolean: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun equals(other: Any?): Boolean {
        val o = other as CheckItem
        return o.content == content
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(content)
        parcel.writeByte(if (isForKitCollection) 1 else 0)
        parcel.writeByte(if (isCheckedBoolean) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckItem> {
        override fun createFromParcel(parcel: Parcel): CheckItem {
            return CheckItem(parcel)
        }

        override fun newArray(size: Int): Array<CheckItem?> {
            return arrayOfNulls(size)
        }
    }
}