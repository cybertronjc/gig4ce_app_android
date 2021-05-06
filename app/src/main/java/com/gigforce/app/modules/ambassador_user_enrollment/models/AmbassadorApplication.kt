package com.gigforce.app.modules.ambassador_user_enrollment.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.core.TranslationNeeded
import com.gigforce.app.utils.lang_models.BaseLangModel

data class AmbassadorApplication(
    @field:TranslationNeeded("bankDetailsText") var bankDetailsText: String = ""
    , @field:TranslationNeeded("currentAddressText") var currentAddressText: String = "",
    @field:TranslationNeeded("profilePhotoText") var profilePhotoText: String = ""
    , @field:TranslationNeeded("subtitle") var subtitle: String = "",
    @field:TranslationNeeded("title") var title: String = "",
    @field:TranslationNeeded("toolbarTitle") var toolbarTitle: String = "",
    @field:TranslationNeeded("actionButtonText") var actionButtonText: String = "",
    @field:TranslationNeeded("stepsText") var stepsText: String = "",
    @field:TranslationNeeded("ambassadorDialogAction") var ambassadorDialogAction: String = "",
    @field:TranslationNeeded("ambassadorDialogSubtitle") var ambassadorDialogSubtitle: String = "",
    @field:TranslationNeeded("ambassadorDialogTitle") var ambassadorDialogTitle: String = "",
    @field:TranslationNeeded("guideLines") var guideLines: List<String> = listOf()


) : BaseLangModel(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bankDetailsText)
        parcel.writeString(currentAddressText)
        parcel.writeString(profilePhotoText)
        parcel.writeString(subtitle)
        parcel.writeString(title)
        parcel.writeString(toolbarTitle)
        parcel.writeString(actionButtonText)
        parcel.writeString(stepsText)
        parcel.writeString(ambassadorDialogAction)
        parcel.writeString(ambassadorDialogSubtitle)
        parcel.writeString(ambassadorDialogTitle)
        parcel.writeStringList(guideLines)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AmbassadorApplication> {
        override fun createFromParcel(parcel: Parcel): AmbassadorApplication {
            return AmbassadorApplication(parcel)
        }

        override fun newArray(size: Int): Array<AmbassadorApplication?> {
            return arrayOfNulls(size)
        }
    }
}