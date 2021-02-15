package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.TextDrawable
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class ChatListItem(context: Context?) :
    RelativeLayout(context),
    IViewHolder,
    View.OnClickListener {

    @Inject
    lateinit var navigation: IChatNavigation

    //Views
    private lateinit var contextImageView: ImageView
    private lateinit var textViewName: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var textViewTime: TextView
    private lateinit var lastMessageType: ImageView
    private lateinit var unseenMessageCountIV: ImageView
    private lateinit var userOnlineIV: ImageView

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    init {

        (this.context.applicationContext as ChatModuleProvider).provideChatModule().inject(this)
        this.navigation.context = this.context
        LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_list, this, true)
        this.findViewById<View>(R.id.contactItemRoot).setOnClickListener(this)

        findViews()
    }

    private fun findViews() {

        contextImageView = this.findViewById(R.id.iv_profile)
        textViewName = this.findViewById(R.id.txt_title)
        txtSubtitle = this.findViewById(R.id.txt_subtitle)
        textViewTime = this.findViewById(R.id.txt_time)
        userOnlineIV = this.findViewById(R.id.user_online_iv)
        lastMessageType = this.findViewById(R.id.last_message_type_iv)
        unseenMessageCountIV = this.findViewById(R.id.unseen_msg_count_iv)
    }

    private var dObj: ChatListItemDataObject? = null

    override fun bind(data: Any?) {
        dObj = null
        data?.let {
            dObj = data as ChatListItemDataObject
            dObj?.let { chatHeader ->

                userOnlineIV.isVisible = chatHeader.isOtherUserOnline

                if (chatHeader.unreadCount != 0) {
                    val drawable = TextDrawable.builder().buildRound(
                        chatHeader.unreadCount.toString(),
                        ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                    )
                    unseenMessageCountIV.setImageDrawable(drawable)
                } else {
                    unseenMessageCountIV.setImageDrawable(null)
                }

                if (chatHeader.type == ChatConstants.CHAT_TYPE_USER) {

                    textViewName.text = chatHeader.title
                    if (chatHeader.profilePath.isNotBlank()) {
                        val profilePathRef = storage.reference.child(chatHeader.profilePath)

                        Glide.with(context)
                            .load(profilePathRef)
                            .placeholder(R.drawable.ic_user)
                            .into(contextImageView)
                    } else {

                        Glide.with(context).load(R.drawable.ic_user).into(contextImageView)
                    }

                } else if (chatHeader.type == ChatConstants.CHAT_TYPE_GROUP) {

                    textViewName.text = chatHeader.groupName
                    val userAvatarUrl = chatHeader.groupAvatar
                    if (userAvatarUrl.isBlank()) {

                        //Show Default User avatar
                        Glide.with(context).load(R.drawable.ic_group).into(contextImageView)
                    } else {

                        val profilePathRef = storage.reference.child(chatHeader.profilePath)
                        Glide.with(context).load(profilePathRef).placeholder(R.drawable.ic_group)
                            .into(contextImageView)
                    }
                }

                when (chatHeader.lastMessageType) {
                    ChatConstants.MESSAGE_TYPE_TEXT -> {
                        lastMessageType.gone()
                        txtSubtitle.text = chatHeader.subtitle
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_play)
                        txtSubtitle.text = "Video"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_document_outlined)
                        txtSubtitle.text = "Document"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_document_outlined)
                        txtSubtitle.text = "Image"
                    }
                    else -> {
                        //   lastMessageType.gone()
                        txtSubtitle.text = ""
                    }
                }

                textViewTime.text = chatHeader.timeDisplay
            }
        }
    }

    override fun onClick(v: View?) {
        dObj?.let {

            navigation.navigateToChatPage(
                chatType = it.chatType,
                otherUserId = it.profileId,
                headerId = it.id,
                otherUserName = it.title,
                otherUserProfilePicture = it.profilePath
            )
        }
    }
}