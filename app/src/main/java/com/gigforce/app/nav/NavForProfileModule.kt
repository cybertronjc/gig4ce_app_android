package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForProfileModule(
    baseImplementation: BaseNavigationImpl
){

    init {
        val moduleName = "profile"
        baseImplementation.registerRoute("${moduleName}", R.id.profileFragment)
        baseImplementation.registerRoute("${moduleName}/addBio", R.id.fragment_add_bio)

    }

}