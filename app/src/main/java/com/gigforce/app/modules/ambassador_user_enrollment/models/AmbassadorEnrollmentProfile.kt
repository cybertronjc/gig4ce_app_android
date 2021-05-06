package com.gigforce.app.modules.ambassador_user_enrollment.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.core.TranslationNeeded
import com.gigforce.app.utils.lang_models.BaseLangModel

data class AmbassadorEnrollmentProfile(
    @TranslationNeeded("actionButtonText") var actionButtonText: String = "",
    @TranslationNeeded("alreadyExistsDialogAction") var alreadyExistsDialogAction: String = "",
    @TranslationNeeded("alreadyExistsDialogContent") var alreadyExistsDialogContent: String = "",
    @TranslationNeeded("alreadyExistsDialogTitle") var alreadyExistsDialogTitle: String = "",
    @TranslationNeeded("countryCodeText") var countryCodeText: String = "",
    @TranslationNeeded("dialogValidationText") var dialogValidationText: String = "",
    @TranslationNeeded("enterUserMobileText") var enterUserMobileText: String = "",
    @TranslationNeeded("noProfileActionText") var noProfileActionText: String = "",
    @TranslationNeeded("noProfileHeaderText") var noProfileHeaderText: String = "",
    @TranslationNeeded("noProfileSubtitleText") var noProfileSubtitleText: String = "",
    @TranslationNeeded("sendOtpText") var sendOtpText: String = "",
    @TranslationNeeded("managingGigsTabText") var managingGigsTabText: String = "",
    @TranslationNeeded("sourcingTabText") var sourcingTabText: String = "",
    @TranslationNeeded("profileTabText") var profileTabText: String = "",
    @TranslationNeeded("enrollmentTitle") var enrollmentTitle: String = "",
    @TranslationNeeded("sentOtpText") var sentOtpText: String = "",
    @TranslationNeeded("enterCodeText") var enterCodeText: String = "",
    @TranslationNeeded("confirmOtpActionText") var confirmOtpActionText: String = "",
    @TranslationNeeded("validOtpText") var validOtpText: String = "",
    @TranslationNeeded("otpConfirmedText") var otpConfirmedText: String = ""
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
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(actionButtonText)
        parcel.writeString(alreadyExistsDialogAction)
        parcel.writeString(alreadyExistsDialogContent)
        parcel.writeString(alreadyExistsDialogTitle)
        parcel.writeString(countryCodeText)
        parcel.writeString(dialogValidationText)
        parcel.writeString(enterUserMobileText)
        parcel.writeString(noProfileActionText)
        parcel.writeString(noProfileHeaderText)
        parcel.writeString(noProfileSubtitleText)
        parcel.writeString(sendOtpText)
        parcel.writeString(managingGigsTabText)
        parcel.writeString(sourcingTabText)
        parcel.writeString(profileTabText)
        parcel.writeString(enrollmentTitle)
        parcel.writeString(sendOtpText)
        parcel.writeString(enterCodeText)
        parcel.writeString(confirmOtpActionText)
        parcel.writeString(validOtpText)
        parcel.writeString(otpConfirmedText)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AmbassadorEnrollmentProfile> {
        override fun createFromParcel(parcel: Parcel): AmbassadorEnrollmentProfile {
            return AmbassadorEnrollmentProfile(parcel)
        }

        override fun newArray(size: Int): Array<AmbassadorEnrollmentProfile?> {
            return arrayOfNulls(size)
        }
    }
}