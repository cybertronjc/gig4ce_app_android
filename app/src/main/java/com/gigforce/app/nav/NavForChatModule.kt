package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForChatModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName:String = "chats"
        baseImplementation.registerRoute("${moduleName}", R.id.contactScreenFragment)
    }
}