package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.gigforce.core.IViewHolder
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import javax.inject.Inject

class ChatListItem(context: Context?) :
    RelativeLayout(context),
    IViewHolder,
    View.OnClickListener {

    @Inject
    lateinit var navigation: IChatNavigation

    init {

        (this.context.applicationContext as ChatModuleProvider).provideChatModule().inject(this)
        this.navigation.context = this.context          //todo: set context through injection only

        LayoutInflater.from(context)
            .inflate(R.layout.chat_list_item, this, true)
        this.setOnClickListener(this)
    }

    private var dObj: ChatListItemDataObject? = null

    override fun bind(data: Any?) {
        // title:String, subtitle:String, timeDisplay:String, profilePath:String, unreadCount:Int, id:String, type: String
        dObj = null
        data?.let {
            dObj = data as ChatListItemDataObject
            val isUnread = dObj!!.unreadCount!! > 0;

            this.findViewById<TextView>(R.id.txt_title).text = dObj?.title
            this.findViewById<TextView>(R.id.txt_subtitle).text = dObj?.subtitle
            this.findViewById<TextView>(R.id.txt_time).text = dObj?.timeDisplay

            //todo: Profile, unreadCount
        }
    }

    override fun onClick(v: View?) {
        dObj?.let {

            navigation.navigateToChatPage(
                    otherUserId = it.profileId,
                    headerId = it.id,
                    otherUserName = it.title,
                    otherUserProfilePicture = it.profilePath
            )
        }
    }
}