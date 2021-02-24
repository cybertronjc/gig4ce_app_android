package com.gigforce.learning.learning.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class LessonModel(
        @get:PropertyName("id")
        @set:PropertyName("id")
        var id : String = "",
        @get:PropertyName("Name")
        @set:PropertyName("Name")
        var name: String = "",
        @get:PropertyName("cover_pic")
        @set:PropertyName("cover_pic")
        var coverPicture: String? = null,
        @get:PropertyName("completed")
        @set:PropertyName("completed")
        var completed: Boolean = false,
        @get:PropertyName("description")
        @set:PropertyName("description")
        var description: String = "",
        var module_id: String = "",
        var lesson_id: String = "",
        var type: String = "",
        var url: String = "",
        @get:PropertyName("feedbackDialogRequire")
        @set:PropertyName("feedbackDialogRequire")
        var shouldShowFeedbackDialog : Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(coverPicture)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeString(description)
        parcel.writeString(module_id)
        parcel.writeString(lesson_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LessonModel> {
        override fun createFromParcel(parcel: Parcel): LessonModel {
            return LessonModel(parcel)
        }

        override fun newArray(size: Int): Array<LessonModel?> {
            return arrayOfNulls(size)
        }
    }
}