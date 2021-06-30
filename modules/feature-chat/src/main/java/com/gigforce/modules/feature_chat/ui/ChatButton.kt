package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.components.atoms.IconButton
import com.gigforce.common_ui.components.atoms.IconType

class ChatButton(context: Context, attrs: AttributeSet?) :
        IconButton(IconType.Chat, context, attrs) {

    init {
        this.setOnClickListener {
            // iChatNavigation.navigateToChatList() todo: Change to New Navigation
        }
    }

}