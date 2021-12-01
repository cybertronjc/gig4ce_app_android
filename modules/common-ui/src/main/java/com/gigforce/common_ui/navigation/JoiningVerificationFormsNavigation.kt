package com.gigforce.common_ui.navigation

import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

class JoiningVerificationFormsNavigation @Inject constructor(
   private val navigation : INavigation
) {

    fun openAadhaarVerificationQuestionnaireForJoiningFragment(
        userId : String,
        jobProfileId : String
    ){
      navigation.navigateTo(JoiningVerificationNavDestinations.JOINING_AADHAAR_VERIFICATION_FRAGMENT)
    }

    fun openBankDetailsVerificationForJoiningFragment(
        userId: String
    ){
        navigation.navigateTo(JoiningVerificationNavDestinations.JOINING_BANK_DETAIL_VERIFICATION_FRAGMENT)
    }

    fun openPanVerificationForJoiningFragment(
        userId: String
    ){
        navigation.navigateTo(JoiningVerificationNavDestinations.JOINING_PAN_VERIFICATION_FRAGMENT)
    }

    fun openDrivingLicenseVerificationForJoiningFragment(
        userId: String
    ){
        navigation.navigateTo(JoiningVerificationNavDestinations.JOINING_DL_VERIFICATION_FRAGMENT)
    }
}