package com.gigforce.giger_app.help

import android.os.Parcel
import android.os.Parcelable

data class HelpSectionDM(var name : String?="",var id : String?="",var questions : ArrayList<HelpDetailSectionDM>?=null):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HelpSectionDM> {
        override fun createFromParcel(parcel: Parcel): HelpSectionDM {
            return HelpSectionDM(parcel)
        }

        override fun newArray(size: Int): Array<HelpSectionDM?> {
            return arrayOfNulls(size)
        }
    }
}
