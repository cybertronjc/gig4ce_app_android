package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavUserDetailsInfo(
    baseImplementation: BaseNavigationImpl
){

    init {

        val moduleName:String = "userinfo"
        baseImplementation.registerRoute("${moduleName}/addCurrentAddressFragment", R.id.addCurrentAddressFragment)
        baseImplementation.registerRoute("${moduleName}/addProfilePictureFragment", R.id.addProfilePictureFragment)
        baseImplementation.registerRoute("${moduleName}/addUserCurrentAddressFragment", R.id.addUserCurrentAddressFragment)
        baseImplementation.registerRoute("${moduleName}/addUserExperienceFragment", R.id.addUserExperienceFragment)
        baseImplementation.registerRoute("${moduleName}/addUserInterestFragment", R.id.addUserInterestFragment)
        baseImplementation.registerRoute("${moduleName}/confirmOtpFragment", R.id.confirmOtpFragment)
        baseImplementation.registerRoute("${moduleName}/addUserDetailsFragment", R.id.addUserDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/addBankDetailsInfoFragment", R.id.addBankDetailsInfoFragment)
        baseImplementation.registerRoute("${moduleName}/checkMobileFragment", R.id.checkMobileFragment)

        // old doc structure data
        baseImplementation.registerRoute("${moduleName}/addUserBankDetailsInfoFragment", R.id.addUserBankDetailsInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addUserDrivingLicenseInfoFragment", R.id.addUserDrivingLicenseInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addUserPanCardInfoFragment", R.id.addUserPanCardInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addUserAadharCardInfoFragment", R.id.addUserAadharCardInfoFragment)

        // new ambassador fragments





    }
}