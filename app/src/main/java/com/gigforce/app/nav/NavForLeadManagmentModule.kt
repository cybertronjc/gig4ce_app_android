package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForLeadManagmentModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "LeadMgmt"
        baseImplementation.registerRoute("${moduleName}/joiningListFragment", R.id.joiningListFragment)
        baseImplementation.registerRoute("${moduleName}/gigerOnboarding", R.id.gigerOnboardingFragment)
        baseImplementation.registerRoute("${moduleName}/gigerOnboardingOtp", R.id.gigerOtpVerification)
        baseImplementation.registerRoute("${moduleName}/selectGigApplicationToActivate", R.id.selectGigApplicationToActivate)
        baseImplementation.registerRoute("${moduleName}/selectTeamLeaders", R.id.selectTeamLeaderFragment)
        baseImplementation.registerRoute("${moduleName}/shiftTimings", R.id.shiftTimingFragment)
        baseImplementation.registerRoute("${moduleName}/selectGigLocation", R.id.selectGigLocationFragment)
    }
}