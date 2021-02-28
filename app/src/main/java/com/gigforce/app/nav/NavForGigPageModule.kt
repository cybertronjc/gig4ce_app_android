package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForGigPageModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName:String = "gig"
        baseImplementation.registerRoute("${moduleName}/mygig", R.id.gig_history_fragment)

        baseImplementation.registerRoute("${moduleName}/attendance", R.id.gigAttendancePageFragment)
    }
}