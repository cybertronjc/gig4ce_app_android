package com.gigforce.app.modules.questionnaire.models

import android.os.Parcel
import android.os.Parcelable

data class QuestionnaireResponse(var type: String = "", var questions: List<Questions> = listOf()) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.createTypedArrayList(Questions) ?: listOf()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeTypedList(questions)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuestionnaireResponse> {
        override fun createFromParcel(parcel: Parcel): QuestionnaireResponse {
            return QuestionnaireResponse(parcel)
        }

        override fun newArray(size: Int): Array<QuestionnaireResponse?> {
            return arrayOfNulls(size)
        }
    }
}