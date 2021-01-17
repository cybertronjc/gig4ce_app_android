package com.gigforce.modules.feature_chat

import android.content.Context
import android.view.View
import com.gigforce.core.*
import com.gigforce.modules.feature_chat.core.ViewTypes
import com.gigforce.modules.feature_chat.ui.ChatListItem
import com.gigforce.modules.feature_chat.ui.chatItems.InTextMessage
import com.gigforce.modules.feature_chat.ui.chatItems.OutTextMessage

class ChatViewTypeLoader(): IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            ViewTypes.CHAT_HEADER -> ChatListItem(context)
            ViewTypes.IN_TEXT -> InTextMessage(context, null)
            ViewTypes.OUT_TEXT -> OutTextMessage(context, null)
            ViewTypes.IN_IMAGE -> InTextMessage(context, null)
            ViewTypes.OUT_IMAGE -> InTextMessage(context, null)
            else -> null
        }
    }
}

