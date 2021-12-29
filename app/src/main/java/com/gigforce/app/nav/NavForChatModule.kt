package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForChatModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "chats"
        baseImplementation.registerRoute("${moduleName}/chatList", R.id.chatListFragment)
        baseImplementation.registerRoute("${moduleName}/chatPage", R.id.chatPageFragment)
        baseImplementation.registerRoute("${moduleName}/groupDetails", R.id.groupDetailsFragment2)
        baseImplementation.registerRoute("${moduleName}/contacts", R.id.contactsFragment)
        baseImplementation.registerRoute("${moduleName}/groupMediaList", R.id.groupMediaListFragment2)
        baseImplementation.registerRoute("${moduleName}/messageInfo", R.id.messageViewingInfoFragment)
        baseImplementation.registerRoute("${moduleName}/chatSettings", R.id.chatSettingsFragment)
        baseImplementation.registerRoute("${moduleName}/syncContacts", R.id.syncContactsBottomSheetFragment)
        baseImplementation.registerRoute("${moduleName}/contactsFragment", R.id.contactsAndGroupFragment)
        baseImplementation.registerRoute("${moduleName}/userGroupDetailsFragment", R.id.userAndGroupDetailsFragment)



//        baseImplementation.registerRoute("${moduleName}/main", R.id.contactScreenFragment)
//        baseImplementation.registerRoute("${moduleName}/chatScreenFragment", R.id.chatScreenFragment)


    }
}