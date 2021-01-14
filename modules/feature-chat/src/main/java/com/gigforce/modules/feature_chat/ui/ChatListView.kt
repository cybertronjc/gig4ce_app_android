package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreRecyclerAdapter
import com.gigforce.core.CoreViewHolder
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject

class ChatListView(context: Context,  attrs: AttributeSet) : RecyclerView(context, attrs) {

    var headers:ArrayList<ChatListItemDataObject> = ArrayList()

    init {
        testInitHeaders()
        fun viewHolderFn(parent:ViewGroup, viewType: Int):CoreViewHolder{
            return CoreViewHolder(ChatListItem(context))
        }
        val adapter: CoreRecyclerAdapter = CoreRecyclerAdapter(context, headers, ::viewHolderFn)
        this.layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
    }

    fun testInitHeaders(){
        for(i in 1..100){
            headers.add(
                ChatListItemDataObject(
                    title = "Chirag Mittal",
                    subtitle = "hi",
                    timeDisplay = "2 min ago",
                    unreadCount = 0,
                    profilePath = "",
                    id="",
                    type = "chat"
                ));
        }
    }
}