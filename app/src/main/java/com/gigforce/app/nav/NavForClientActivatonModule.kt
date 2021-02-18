package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForClientActivatonModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "client_activation"
        baseImplementation.registerRoute("${moduleName}", R.id.fragment_client_activation)
    }
}