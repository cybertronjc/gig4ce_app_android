package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.core.CoreRecyclerView
import com.gigforce.modules.feature_chat.models.ChatItemDataObject
import com.gigforce.modules.feature_chat.models.Message

class ChatRecyclerView(context: Context, attrs: AttributeSet) : CoreRecyclerView(context, attrs) {

    private var _messages:ArrayList<Message> = ArrayList()
    var messages:ArrayList<Message>
        get() = _messages
        set(value){
            _messages = value
            this.coreAdapter.collection = _messages
        }

    init {
        this.setDefaultAdapter(messages, ::ChatItem)
    }
}