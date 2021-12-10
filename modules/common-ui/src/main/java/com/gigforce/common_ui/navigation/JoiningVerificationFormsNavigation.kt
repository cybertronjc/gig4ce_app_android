package com.gigforce.common_ui.navigation

import androidx.core.os.bundleOf
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.AppConstants
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class JoiningVerificationFormsNavigation @Inject constructor(
    private val navigation: INavigation
) {

    fun openAadhaarVerificationQuestionnaireForJoiningFragment(
        userId: String,
        jobProfileId: String
    ) {
        navigation.navigateTo(
            JoiningVerificationNavDestinations.VERIFICATION_AADHAAR,
            bundleOf(
                AppConstants.INTENT_EXTRA_UID to userId,
                StringConstants.JOB_PROFILE_ID.value to jobProfileId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openBankDetailsVerificationForJoiningFragment(
        userId: String
    ) {
        navigation.navigateTo(
            JoiningVerificationNavDestinations.VERIFICATION_BANK_DETAILS,
            bundleOf(
                AppConstants.INTENT_EXTRA_UID to userId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openPanVerificationForJoiningFragment(
        userId: String
    ) {
        navigation.navigateTo(
            JoiningVerificationNavDestinations.VERIFICATION_PAN_CARD,
            bundleOf(
                AppConstants.INTENT_EXTRA_UID to userId
            ),
            NavigationOptions.getNavOptions()
        )
    }

    fun openDrivingLicenseVerificationForJoiningFragment(
        userId: String
    ) {
        navigation.navigateTo(
            JoiningVerificationNavDestinations.VERIFICATION_DRIVER_LICENSE,
            bundleOf(
                AppConstants.INTENT_EXTRA_UID to userId
            ),
            NavigationOptions.getNavOptions()
        )
    }
}