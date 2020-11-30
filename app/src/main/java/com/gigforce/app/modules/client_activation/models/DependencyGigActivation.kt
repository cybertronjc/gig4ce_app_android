package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class DependencyGigActivation(var docType: String = "", var title: String = "", var type: String = "",
                                   var trainingData: List<String> = listOf(), var drawable: Int = -1, var isDone: Boolean = false) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(docType)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeInt(drawable)
        parcel.writeStringList(trainingData)
        parcel.writeByte(if (isDone) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DependencyGigActivation> {
        override fun createFromParcel(parcel: Parcel): DependencyGigActivation {
            return DependencyGigActivation(parcel)
        }

        override fun newArray(size: Int): Array<DependencyGigActivation?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        val obj = other as DependencyGigActivation
        return obj.docType == docType
    }

}