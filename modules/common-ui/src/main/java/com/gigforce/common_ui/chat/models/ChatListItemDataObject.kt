package com.gigforce.common_ui.chat.models

import android.os.Bundle
import com.gigforce.core.SimpleDVM
//import com.gigforce.modules.feature_chat.core.ViewTypes

data class ChatListItemDataWrapper(
        val chatItem : ChatListItemDataObject,
        val sharedFilesBundle : Bundle?
): SimpleDVM(ViewTypes.CHAT_HEADER)

data class ChatListItemDataObject(
    val chatType:String,
    val title:String,
    val subtitle:String,
    val timeDisplay:String,

    val profileId:String,
    val profilePath:String,

    val unreadCount:Int,
    val id:String,
    val type: String,

    val isOtherUserOnline: Boolean,

    val groupName: String,
    val groupAvatar: String,

    val lastMessage: String,
    val lastMessageType: String,
    val lastMsgFlowType: String,

    val senderName : String,

    val status : Int

)