package com.gigforce.app.navigation.tl_workspace

import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkSpaceNavigation @Inject constructor(
    private val navigation: INavigation
) {
    companion object {

        const val NAV_DESTINATION_TL_WORKSPACE_HOME = "tl_workspace/home"
    }

    fun navigateToTLWorkSpaceHomeScreen() {
        navigation.navigateTo(
            NAV_DESTINATION_TL_WORKSPACE_HOME,
            null,
            NavigationOptions.getNavOptions()
        )
    }
}