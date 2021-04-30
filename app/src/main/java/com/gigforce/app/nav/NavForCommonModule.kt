package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForCommonModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "common"
        baseImplementation.registerRoute("${moduleName}/viewVideoFullScreen", R.id.viewFullScreenVideoDialogFragment)
        baseImplementation.registerRoute("${moduleName}/viewImageFullScreen", R.id.viewFullScreenImageDialogFragment)
        baseImplementation.registerRoute("${moduleName}/invite_friend", R.id.referrals_fragment)


    }
}