package com.gigforce.giger_app.datamodel

data class AppConfigsDM(
    val active: Boolean = true,
    val borderCorner: Long = 4,
    val borderShadowRadius: Long = 4,
    val borderWidth: Long = 2,
    val borderWidthColor: String = "#00942c",
    val icon: String = "chat",
    val index: Long = 150,
    val navPath: String = "chats/chatList",
    val title: String = "Chat",
    val type: String? = null,
    val parentIcon: String? = null
)