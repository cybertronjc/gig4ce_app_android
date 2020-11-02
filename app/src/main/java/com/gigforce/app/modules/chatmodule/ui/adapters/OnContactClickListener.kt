package com.gigforce.app.modules.chatmodule.ui.adapters

import com.gigforce.app.modules.chatmodule.models.ChatHeader

interface OnContactClickListener {
    fun contactClick(chatHeader: ChatHeader)
}