package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import com.gigforce.core.CoreRecyclerView
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject

class ChatListView(context: Context,  attrs: AttributeSet) : CoreRecyclerView(context, attrs) {

    private var _headers:ArrayList<ChatListItemDataObject> = ArrayList()
    var headers:ArrayList<ChatListItemDataObject>
        get() = _headers
        set(value){
            _headers = value
            this.coreAdapter.collection = _headers
        }

    init {
        this.setDefaultAdapter(headers, ::ChatListItem)
    }
}