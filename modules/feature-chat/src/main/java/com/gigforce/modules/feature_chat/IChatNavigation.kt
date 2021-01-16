package com.gigforce.modules.feature_chat

import android.content.Context
import com.gigforce.core.navigation.INavigation

interface IChatNavigation: INavigation {

    fun navigateToChatList()
    fun navigateToChatPage(id:String)
}