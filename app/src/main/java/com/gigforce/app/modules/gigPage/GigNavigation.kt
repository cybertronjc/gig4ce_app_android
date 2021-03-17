package com.gigforce.app.modules.gigPage

import android.os.Bundle
import androidx.navigation.NavController
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage2.GigPage2Fragment

object GigNavigation {

    fun openGigAttendancePage(navController: NavController,
                              openNewGigPage: Boolean,
                              gigId: String) {
        openGigAttendancePage(navController, openNewGigPage, Bundle().apply {
            this.putString(GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
        })
    }

    fun openGigAttendancePage(navController: NavController,
                              openNewGigPage: Boolean,
                              extras: Bundle?) {

        if (openNewGigPage)
            navController.navigate(R.id.gigPage2Fragment, extras)
        else
            navController.navigate(R.id.gigAttendancePageFragment, extras)
    }

    fun openGigMainPage(navController: NavController,
                        openNewGigPage: Boolean,
                        gigId: String) {

        openGigMainPage(navController, openNewGigPage, Bundle().apply {
            this.putString(GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
        })

    }

    fun openGigMainPage(navController: NavController,
                        openNewGigPage: Boolean,
                        bundle: Bundle?) {

        if (openNewGigPage)
            navController.navigate(R.id.gigPage2Fragment, bundle)
        else
            navController.navigate(R.id.presentGigPageFragment, bundle)
    }
}