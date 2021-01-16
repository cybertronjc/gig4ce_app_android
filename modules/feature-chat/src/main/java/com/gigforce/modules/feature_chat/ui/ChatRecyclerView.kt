package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.core.CoreRecyclerView
import com.gigforce.modules.feature_chat.models.ChatItemDataObject

class ChatRecyclerView(context: Context, attrs: AttributeSet) : CoreRecyclerView(context, attrs) {

    private var _messages:ArrayList<ChatItemDataObject> = ArrayList()
    var messages:ArrayList<ChatItemDataObject>
        get() = _messages
        set(value){
            _messages = value
            this.coreAdapter.collection = _messages
        }

    init {
        this.setDefaultAdapter(messages, ::ChatItem)
    }
}