package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.core.CoreRecyclerView
import com.gigforce.modules.feature_chat.models.ChatItemDataObject

class ChatRecyclerView(context: Context, attrs: AttributeSet) : CoreRecyclerView(context, attrs) {

    private var _chatItems:ArrayList<ChatItemDataObject> = ArrayList()
    var headers:ArrayList<ChatItemDataObject>
        get() = _chatItems
        set(value){
            _chatItems = value
            this.coreAdapter.collection = _chatItems
        }

    init {
        this.setDefaultAdapter(headers, ::ChatItemIn)
    }
}