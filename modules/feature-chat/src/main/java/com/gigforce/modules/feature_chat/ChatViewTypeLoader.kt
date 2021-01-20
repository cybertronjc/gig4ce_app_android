package com.gigforce.modules.feature_chat

import android.content.Context
import android.view.View
import com.gigforce.core.*
import com.gigforce.modules.feature_chat.core.ViewTypes
import com.gigforce.modules.feature_chat.ui.ChatListItem
import com.gigforce.modules.feature_chat.ui.chatItems.*

class ChatViewTypeLoader(): IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            ViewTypes.CHAT_HEADER -> ChatListItem(context)
            ViewTypes.IN_TEXT -> InTextMessageView(context, null)
            ViewTypes.OUT_TEXT -> OutTextMessageView(context, null)
            ViewTypes.IN_IMAGE -> InImageMessageView(context, null)
            ViewTypes.OUT_IMAGE -> OutImageMessageView(context, null)
            ViewTypes.IN_VIDEO -> InVideoMessageView(context, null)
            ViewTypes.OUT_VIDEO -> OutVideoMessageView(context, null)
            else -> null
        }
    }
}

