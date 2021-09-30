package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.lead_management.LeadManagementNavDestinations

class NavForLeadManagmentModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_JOINING, R.id.joiningList2Fragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING, R.id.gigerOnboardingFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING_OTP, R.id.gigerOtpVerification)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_TO_ACTIVATE, R.id.selectGigApplicationToActivate)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_TEAM_LEADERS, R.id.selectTeamLeaderFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_SHIFT_TIMMINGS, R.id.shiftTimingFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_LOCATION, R.id.selectGigLocationFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_REFERENCE_CHECK, R.id.referenceCheckFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_PICK_PROFILE_FOR_REFERRAL, R.id.pickProfileForReferralFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_REFERRAL, R.id.shareReferralLinkFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_GIGER_INFO, R.id.gigerInfoFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1, R.id.newSelectionForm1Fragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_BUSINESS, R.id.selectBusinessFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_JOB_PROFILE, R.id.selectJobProfileFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2, R.id.newSelectionForm2Fragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_CITY, R.id.selectCityFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_REPORTING_LOCATION, R.id.selectReportingLocationFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_CLIENT_TL, R.id.selectClientTlFragment)
        baseImplementation.registerRoute(LeadManagementNavDestinations.FRAGMENT_SELECT_FORM_SUCCESS, R.id.newSelectionFormSubmitSuccessFragment)
    }
}