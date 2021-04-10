package com.gigforce.app.modules.gigPage2

import android.os.Bundle
import androidx.navigation.NavController
import com.gigforce.app.R

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

        navController.navigate(R.id.gigPage2Fragment, extras)
    }

    fun openGigMainPage(
            navController: NavController,
            openNewGigPage: Boolean,
            gigId: String
    ) {

        openGigMainPage(navController, openNewGigPage, Bundle().apply {
            this.putString(GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
        })

    }

    fun openGigMainPage(navController: NavController,
                        openNewGigPage: Boolean,
                        bundle: Bundle?) {
        navController.navigate(R.id.gigPage2Fragment, bundle)
    }
}