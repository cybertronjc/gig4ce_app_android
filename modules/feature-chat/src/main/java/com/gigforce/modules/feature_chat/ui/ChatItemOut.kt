package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.gigforce.core.IViewHolder
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject

class ChatItemOut(context: Context?) :
        RelativeLayout(context),
        IViewHolder,
        View.OnClickListener
{

    init {
        LayoutInflater.from(context).inflate(R.layout.chat_item_out, this, true)
        this.setOnClickListener(this)
    }

    private var dObj:ChatListItemDataObject? = null

    override fun bind(data:Any?){
        // title:String, subtitle:String, timeDisplay:String, profilePath:String, unreadCount:Int, id:String, type: String
        dObj = null
        data?.let{
            dObj = data as ChatListItemDataObject
            val isUnread = dObj?.unreadCount!! > 0;

            this.findViewById<TextView>(R.id.txt_title).text = dObj?.title;
            this.findViewById<TextView>(R.id.txt_subtitle).text = dObj?.subtitle;
            this.findViewById<TextView>(R.id.txt_time).text = dObj?.timeDisplay;

            //todo: Profile, unreadCount
        }
    }

    override fun onClick(v: View?) {
        dObj.let {
            Toast.makeText(this.context, "Tapped", Toast.LENGTH_SHORT).show()
        }
    }
}