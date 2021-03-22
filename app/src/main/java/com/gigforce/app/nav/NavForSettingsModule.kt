package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForSettingsModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName = "setting"
        baseImplementation.registerRoute("${moduleName}", R.id.settingFragment)
    }
}