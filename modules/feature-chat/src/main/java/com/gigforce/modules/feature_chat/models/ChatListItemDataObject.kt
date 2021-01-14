package com.gigforce.modules.feature_chat.models

data class ChatListItemDataObject(
    val title:String,
    val subtitle:String,
    val timeDisplay:String,
    val profilePath:String,
    val unreadCount:Int,
    val id:String,
    val type: String
) {}