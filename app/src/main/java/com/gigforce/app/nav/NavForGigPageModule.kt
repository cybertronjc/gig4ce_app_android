package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForGigPageModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName:String = "gig"
        baseImplementation.registerRoute("${moduleName}/mygig", R.id.gig_history_fragment)

        baseImplementation.registerRoute("${moduleName}/attendance", R.id.gigPage2Fragment)
        baseImplementation.registerRoute("${moduleName}/gigRegulariseAttendanceFragment",R.id.gigRegulariseAttendanceFragment)
        baseImplementation.registerRoute("${moduleName}/gigDetailsFragment",R.id.gigDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/gigerIdFragment",R.id.giger_id_fragment)
        baseImplementation.registerRoute("${moduleName}/gigMonthlyAttendanceFragment",R.id.gigMonthlyAttendanceFragment)
        baseImplementation.registerRoute("${moduleName}/gigerAttendanceUnderManagerFragment",R.id.gigerAttendanceUnderManagerFragment)
        baseImplementation.registerRoute("${moduleName}/tlLoginDetails",R.id.teamLeaderLoginDetailsFragment)
        baseImplementation.registerRoute("${moduleName}/addNewLoginSummary",R.id.addNewLoginSummaryFragment)
        baseImplementation.registerRoute("${moduleName}/imageCropFragment",R.id.imageCropFragment)

    }
}