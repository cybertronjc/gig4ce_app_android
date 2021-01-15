package com.gigforce.modules.feature_chat.models

data class ChatItemDataObject(
    val title:String,
    val subtitle:String,
    val timeDisplay:String,
    val profilePath:String,
    val unreadCount:Int,
    val id:String,
    val type: String
) {}