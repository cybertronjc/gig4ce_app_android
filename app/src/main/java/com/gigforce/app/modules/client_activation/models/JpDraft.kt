package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.app.modules.questionnaire.models.Questions

data class JpDraft(@JvmField var isDone: Boolean = false, var title: String = "", var type: String = "", var questionnaireData
: List<Questions>? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readString() ?: "", null) {
    }

    override fun equals(other: Any?): Boolean {
        val jpDraft = other as JpDraft
        return jpDraft.type == type
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