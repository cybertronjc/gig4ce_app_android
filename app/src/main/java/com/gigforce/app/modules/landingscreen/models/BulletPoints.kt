package com.gigforce.app.modules.landingscreen.models

import android.os.Parcel
import android.os.Parcelable

data class BulletPoints(
    var showPoints: Int = 0, @JvmField var requiredShowPoints: Boolean = false,
    var pointsData: List<String>? = null,
    var title: String? = null,
    var url: String? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(showPoints)
        parcel.writeByte(if (requiredShowPoints) 1 else 0)
        parcel.writeStringList(pointsData)
        parcel.writeString(title)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BulletPoints> {
        override fun createFromParcel(parcel: Parcel): BulletPoints {
            return BulletPoints(parcel)
        }

        override fun newArray(size: Int): Array<BulletPoints?> {
            return arrayOfNulls(size)
        }
    }
}