package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class DocReceiving(
    var title: String = "",
    var subtitle: String = "",
    var alertMessage: String = "",
    var otpLabel: String = "",
    var noteMsg: String = "",
    var checkItems: List<CheckItem> = listOf(),
    var jobProfileId: String = "",
    var type: String = "",
    var dialogTitle: String = "",
    var dialogSubtitle: String = "",
    var dialogContent: List<String> = listOf(),
    var dialogActionMain: String = "",
    var dialogActionSec: String = "",
    var dialogIllustration: String = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(CheckItem) ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(alertMessage)
        parcel.writeString(otpLabel)
        parcel.writeTypedList(checkItems)
        parcel.writeString(jobProfileId)
        parcel.writeString(type)
        parcel.writeString(dialogTitle)
        parcel.writeString(dialogSubtitle)
        parcel.writeStringList(dialogContent)
        parcel.writeString(dialogActionMain)
        parcel.writeString(dialogActionSec)
        parcel.writeString(dialogIllustration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DocReceiving> {
        override fun createFromParcel(parcel: Parcel): DocReceiving {
            return DocReceiving(parcel)
        }

        override fun newArray(size: Int): Array<DocReceiving?> {
            return arrayOfNulls(size)
        }
    }
}