package com.gigforce.modules.feature_chat.models

import com.gigforce.core.SimpleDataViewObject
import com.gigforce.modules.feature_chat.ViewTypes

data class ChatListItemDataObject(
    val title:String,
    val subtitle:String,
    val timeDisplay:String,
    val profilePath:String,
    val unreadCount:Int,
    val id:String,
    val type: String
): SimpleDataViewObject(ViewTypes.CHAT_HEADER) {}