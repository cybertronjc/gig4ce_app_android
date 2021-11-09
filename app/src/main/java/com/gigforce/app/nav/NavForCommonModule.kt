package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.core.documentFileHelper.RequestDocumentTreeAccessFragment

class NavForCommonModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "common"
        baseImplementation.registerRoute("${moduleName}/viewVideoFullScreen", R.id.viewFullScreenVideoDialogFragment)
        baseImplementation.registerRoute("${moduleName}/viewImageFullScreen", R.id.viewFullScreenImageDialogFragment)
        baseImplementation.registerRoute("${moduleName}/invite_friend", R.id.referrals_fragment)
        baseImplementation.registerRoute("${moduleName}/landingScreen", R.id.landinghomefragment)
        baseImplementation.registerRoute("${moduleName}/calendarScreen", R.id.mainHomeScreen)
        baseImplementation.registerRoute(
            RequestDocumentTreeAccessFragment.REQUEST_DOCUMENT_TREE_ACCESS_FRAGMENT,
            R.id.requestDocumentTreeAccessFragment
        )
    }
}