package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForTLReportModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName:String = "tlReport"
        baseImplementation.registerRoute("${moduleName}/addLoginReportFragment", R.id.addLoginReportFragment)
        baseImplementation.registerRoute("${moduleName}/loginReportListFragment", R.id.loginReportListFragment)
    }
}