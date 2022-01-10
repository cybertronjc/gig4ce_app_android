package com.gigforce.modules.feature_chat

import android.content.Context
import android.view.View
import com.gigforce.core.*
import com.gigforce.common_ui.chat.models.ViewTypes
import com.gigforce.modules.feature_chat.ui.ChatListItem
import com.gigforce.modules.feature_chat.ui.GroupMessageReadItemView
import com.gigforce.modules.feature_chat.ui.chatItems.*

class ChatViewTypeLoader: IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            ViewTypes.CHAT_HEADER -> ChatListItem(context)

            ViewTypes.IN_TEXT -> InTextMessageView(context, null)
            ViewTypes.OUT_TEXT -> OutTextMessageView(context, null)

            ViewTypes.IN_IMAGE -> InImageMessageView(context, null)
            ViewTypes.OUT_IMAGE -> OutImageMessageView(context, null)

            ViewTypes.IN_VIDEO -> InVideoMessageView(context, null)
            ViewTypes.OUT_VIDEO -> OutVideoMessageView(context, null)

            ViewTypes.IN_DOCUMENT -> InOneToOneDocumentMessageView(context, null)
            ViewTypes.OUT_DOCUMENT -> OutOneToOneDocumentMessageView(context, null)

            ViewTypes.IN_LOCATION -> InLocationMessageView(context, null)
            ViewTypes.OUT_LOCATION -> OutLocationMessageView(context, null)

            ViewTypes.IN_AUDIO -> InOneToOneAudioMessageView(context, null)
            ViewTypes.OUT_AUDIO -> OutOneToOneAudioMessageView(context, null)

            ViewTypes.IN_DELETED_MESSAGE -> InDeletedMessageView(context, null)
            ViewTypes.OUT_DELETED_MESSAGE -> OutDeletedMessageView(context, null)

            ViewTypes.GROUP_IN_TEXT -> GroupInTextMessageView(context, null)
            ViewTypes.GROUP_OUT_TEXT -> GroupOutTextMessageView(context, null)

            ViewTypes.GROUP_IN_IMAGE -> GroupInImageMessageView(context, null)
            ViewTypes.GROUP_OUT_IMAGE -> GroupOutImageMessageView(context, null)

            ViewTypes.GROUP_IN_VIDEO -> GroupInVideoMessageView(context, null)
            ViewTypes.GROUP_OUT_VIDEO -> GroupOutVideoMessageView(context, null)

            ViewTypes.GROUP_IN_DOCUMENT -> GroupInDocumentMessageView(context, null)
            ViewTypes.GROUP_OUT_DOCUMENT -> GroupOutDocumentMessageView(context, null)

            ViewTypes.GROUP_IN_AUDIO -> GroupInAudioMessageView(context, null)
            ViewTypes.GROUP_OUT_AUDIO -> GroupOutAudioMessageView(context, null)

            ViewTypes.GROUP_IN_LOCATION -> GroupInLocationMessageView(context, null)
            ViewTypes.GROUP_OUT_LOCATION -> GroupOutLocationMessageView(context, null)

            ViewTypes.GROUP_IN_DELETED_MESSAGE -> GroupInDeletedMessageView(context, null)
            ViewTypes.GROUP_OUT_DELETED_MESSAGE -> GroupOutDeletedMessageView(context, null)

            ViewTypes.GROUP_MESSAGE_READ_INFO -> GroupMessageReadItemView(context)

            ViewTypes.CHAT_EVENT -> ChatEventView(context,null)

            ViewTypes.GROUP_DETAILS_GROUP_MEMBER -> TODO()
            ViewTypes.GROUP_DETAILS_MEDIA -> TODO()

            else -> null
        }
    }
}

