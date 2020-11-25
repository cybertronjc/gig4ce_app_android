package com.gigforce.app.modules.questionnaire.models

import android.os.Parcel
import android.os.Parcelable

data class Options(@field:JvmField var is_answer: Boolean = false, var question: String = "", var type: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString()?:"",
            parcel.readString()?:"") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (is_answer) 1 else 0)
        parcel.writeString(question)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Options> {
        override fun createFromParcel(parcel: Parcel): Options {
            return Options(parcel)
        }

        override fun newArray(size: Int): Array<Options?> {
            return arrayOfNulls(size)
        }
    }
}