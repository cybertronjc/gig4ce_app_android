package com.gigforce.common_ui.navigation.gig

import androidx.core.os.bundleOf
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class GigNavigation @Inject constructor(
    private val navigation : INavigation
) {

    companion object{

        const val NAV_DESTINATION_GIG_PAGE  = "gig/attendance"
    }

    fun openGigPage(
        gigId : String
    ) = navigation.navigateTo(
        NAV_DESTINATION_GIG_PAGE,
        bundleOf("gig_id" to gigId),
        NavigationOptions.getNavOptions()
    )
}