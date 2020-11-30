package com.gigforce.app.modules.landingscreen.models

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

data class Dependency(
        var feature: String? = null,
        var priority: Int = 0,
        var title: String? = null,
        var drawable: Drawable? = null,
        var isDone: Boolean = false

) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            null,
            parcel.readByte() != 0.toByte()) {
    }

    override fun equals(other: Any?): Boolean {
        val dependency = other as Dependency
        return dependency.feature == feature

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(feature)
        parcel.writeInt(priority)
        parcel.writeString(title)
        parcel.writeByte(if (isDone) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dependency> {
        override fun createFromParcel(parcel: Parcel): Dependency {
            return Dependency(parcel)
        }

        override fun newArray(size: Int): Array<Dependency?> {
            return arrayOfNulls(size)
        }
    }
}