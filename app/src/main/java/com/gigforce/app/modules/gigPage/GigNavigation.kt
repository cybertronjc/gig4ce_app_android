package com.gigforce.app.modules.gigPage

import android.os.Bundle
import androidx.navigation.NavController
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage2.GigPage2Fragment

object GigNavigation {

    fun openGigAttendancePage(navController: NavController, gigId : String ){
        openGigAttendancePage(navController, Bundle().apply {
            this.putString(GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
        })
    }

    fun openGigAttendancePage(navController: NavController, extras : Bundle?){
        //navController.navigate(R.id.gigAttendancePageFragment, extras)
        navController.navigate(R.id.gigPage2Fragment, extras)
    }

    fun openGigMainPage(navController: NavController, gigId : String){
        openGigMainPage(navController, Bundle().apply {
            this.putString(GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
        })
    }

    fun openGigMainPage(navController: NavController, bundle  : Bundle?){
        //navController.navigate(R.id.presentGigPageFragment, bundle)
        navController.navigate(R.id.gigPage2Fragment, bundle)
    }
}