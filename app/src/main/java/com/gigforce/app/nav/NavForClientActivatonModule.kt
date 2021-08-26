package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForClientActivatonModule(
    baseImplementation: BaseNavigationImpl
) {
    init {
        val moduleName: String = "client_activation"
        baseImplementation.registerRoute("${moduleName}", R.id.fragment_client_activation)
        baseImplementation.registerRoute(
            "${moduleName}/gigActivation",
            R.id.fragment_gig_activation
        )
        baseImplementation.registerRoute(
            "${moduleName}/applicationSubmission",
            R.id.application_submitted_fragment
        )
        baseImplementation.registerRoute(
            "${moduleName}/applicationClientActivation",
            R.id.fragment_application_client_activation
        )
        baseImplementation.registerRoute("${moduleName}/schedule_test", R.id.fragment_schedule_test)
        baseImplementation.registerRoute("${moduleName}/doc_sub_doc", R.id.fragment_doc_sub)
        baseImplementation.registerRoute("${moduleName}/gig_detail", R.id.clientActiExploreList)
        baseImplementation.registerRoute(
            "${moduleName}/aadharDetailsQuestionnaireFragment",
            R.id.aadharApplicationDetailsFragment
        )

        baseImplementation.registerRoute(
            "${moduleName}/fragment_business_loc_hub",
            R.id.fragment_business_loc_hub
        )

        baseImplementation.registerRoute("${moduleName}/joining_form", R.id.fragment_joining_form)

    }
}