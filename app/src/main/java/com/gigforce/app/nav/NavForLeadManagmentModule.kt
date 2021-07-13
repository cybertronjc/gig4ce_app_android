package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForLeadManagmentModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "LeadMgmt"
        baseImplementation.registerRoute("${moduleName}/joiningListFragment", R.id.joiningListFragment)
    }
}