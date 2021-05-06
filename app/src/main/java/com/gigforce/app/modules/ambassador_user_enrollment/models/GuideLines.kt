package com.gigforce.app.modules.ambassador_user_enrollment.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.core.TranslationNeeded
import com.gigforce.app.utils.lang_models.BaseLangModel

data class GuideLines(@field:TranslationNeeded("text") var text: String = "") : BaseLangModel(),
    Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GuideLines> {
        override fun createFromParcel(parcel: Parcel): GuideLines {
            return GuideLines(parcel)
        }

        override fun newArray(size: Int): Array<GuideLines?> {
            return arrayOfNulls(size)
        }
    }
}