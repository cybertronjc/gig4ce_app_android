package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.IViewHolder
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import java.lang.Exception

class ChatListItem(context: Context?) : RelativeLayout(context), IViewHolder {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.chat_list_item, this, true);
    }

    override fun bind(data:Any?){
        // title:String, subtitle:String, timeDisplay:String, profilePath:String, unreadCount:Int, id:String, type: String
        data?.let{
            val dObj = data as ChatListItemDataObject;
            val isUnread = dObj.unreadCount > 0;

            this.findViewById<TextView>(R.id.txt_title).text = dObj.title;
            this.findViewById<TextView>(R.id.txt_subtitle).text = dObj.subtitle;
            this.findViewById<TextView>(R.id.txt_time).text = dObj.timeDisplay;

            //todo: Profile, unreadCount
        }
    }
}