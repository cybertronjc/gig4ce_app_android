package com.gigforce.app.modules.landingscreen.models

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class Dependency(
        var docType: String = "",
        var type: String? = null,
        var title: String? = null,
        var status: String = "",
        var courseId: String = "",
        var moduleId: String = "",
        var lessonId: String = "",
        @JvmField
        var isSlotBooked: Boolean = false,

        @JvmField
        var isDone: Boolean = false,

        @get:Exclude
        var drawable: Drawable? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
    )

    override fun equals(other: Any?): Boolean {
        val dependency = other as Dependency
        return dependency.type == type
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(docType)
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(status)
        parcel.writeString(courseId)
        parcel.writeString(moduleId)
        parcel.writeString(lessonId)
        parcel.writeByte(if (isSlotBooked) 1 else 0)
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