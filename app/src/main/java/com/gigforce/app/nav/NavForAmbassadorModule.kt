package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForAmbassadorModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "ambassador"
        baseImplementation.registerRoute("${moduleName}/self_enrolled", R.id.ambassadorProgramDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/users_enrolled", R.id.gigerPayoutFragment)
        baseImplementation.registerRoute("${moduleName}/ambassadorEnrollmentRequirementFragment", R.id.ambassadorEnrollmentRequirementFragment)
        baseImplementation.registerRoute("${moduleName}/learningCourseDetails", R.id.learningCourseDetails)
        baseImplementation.registerRoute("${moduleName}/UserAadharConfirmationBS", R.id.UserAadharConfirmationBS)



    }
}