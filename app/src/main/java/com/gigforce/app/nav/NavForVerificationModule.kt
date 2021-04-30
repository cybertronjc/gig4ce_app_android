package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForVerificationModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "verification"
        baseImplementation.registerRoute("${moduleName}/main", R.id.gigerVerificationFragment)
    }
}