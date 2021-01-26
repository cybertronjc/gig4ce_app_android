package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.atoms.IconButton
import com.gigforce.common_ui.atoms.IconType
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import javax.inject.Inject

class ChatButton(context: Context, attrs: AttributeSet?) :
        IconButton(IconType.Chat, context, attrs) {

    @Inject
    lateinit var iChatNavigation: IChatNavigation

    init {
        (this.context.applicationContext as ChatModuleProvider).provideChatModule().inject(this)

        this.setOnClickListener {
            iChatNavigation.navigateToChatList()
        }
    }

}