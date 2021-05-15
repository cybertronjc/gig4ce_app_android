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
        baseImplementation.registerRoute("${moduleName}/addUserBankDetailsInfoFragment", R.id.addUserBankDetailsInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addUserDrivingLicenseInfoFragment", R.id.addUserDrivingLicenseInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addUserPanCardInfoFragment", R.id.addUserPanCardInfoFragment)



    }
}