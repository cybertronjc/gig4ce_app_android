package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.common_ui.BaseNavigationImpl

class NavForTlWorkSpace(
    baseImplementation: BaseNavigationImpl
){

    init {
        baseImplementation.registerRoute(
            TLWorkSpaceNavigation.NAV_DESTINATION_TL_WORKSPACE_HOME,
            R.id.tLWorkspaceHomeFragment
        )

        baseImplementation.registerRoute(
            TLWorkSpaceNavigation.NAV_DESTINATION_UPCOMING_GIGERS,
            R.id.upcomingGigersFragment
        )

        baseImplementation.registerRoute(
            TLWorkSpaceNavigation.NAV_DESTINATION_PENDING_COMPLIANCE,
            R.id.compliancePendingFragment
        )

        baseImplementation.registerRoute(
            TLWorkSpaceNavigation.NAV_DESTINATION_RETENTION,
            R.id.retentionFragment
        )
    }
}