package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.net.Uri
import android.util.Patterns
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
import com.gigforce.modules.feature_chat.models.ChatMessage
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class ChatListItem(
    context: Context
) :
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
    private lateinit var statusIV: ImageView

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
        statusIV = this.findViewById(R.id.tv_received_status)
    }

    private var dObj: ChatListItemDataObject? = null

    override fun bind(data: Any?) {
        dObj = null
        data?.let {
            dObj = data as ChatListItemDataObject
            dObj?.let { chatHeader ->

                userOnlineIV.isVisible = chatHeader.isOtherUserOnline
                statusIV.isVisible = chatHeader.lastMsgFlowType == ChatConstants.FLOW_TYPE_OUT && chatHeader.chatType == ChatConstants.CHAT_TYPE_USER

                if (chatHeader.unreadCount != 0) {
                    val drawable = TextDrawable.builder().buildRound(
                            chatHeader.unreadCount.toString(),
                            ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                    )
                    unseenMessageCountIV.setImageDrawable(drawable)
                    textViewName.setTextColor(
                        ResourcesCompat.getColor(context.resources,
                        R.color.lipstick,
                            null
                            )
                    )
                } else {
                    textViewName.setTextColor(
                        ResourcesCompat.getColor(context.resources,
                            R.color.dove_grey,
                            null
                        )
                    )
                    unseenMessageCountIV.setImageDrawable(null)
                }

                if (chatHeader.type == ChatConstants.CHAT_TYPE_USER) {

                    textViewName.text = chatHeader.title
                    if (chatHeader.profilePath.isNotBlank()) {

                        if (Patterns.WEB_URL.matcher(chatHeader.profilePath).matches()) {

                            Glide.with(context)
                                    .load(Uri.parse(chatHeader.profilePath))
                                    .placeholder(R.drawable.ic_user)
                                    .into(contextImageView)
                        } else {

                            val profilePathRef = if (chatHeader.profilePath.startsWith("profile_pics/"))
                                storage.reference.child(chatHeader.profilePath)
                            else
                                storage.reference.child("profile_pics/${chatHeader.profilePath}")

                            Glide.with(context)
                                    .load(profilePathRef)
                                    .placeholder(R.drawable.ic_user)
                                    .into(contextImageView)
                        }
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

                val messagePrefix = if(chatHeader.senderName.isNotBlank())  "${chatHeader.senderName} :" else ""
                when (chatHeader.lastMessageType) {
                    ChatConstants.MESSAGE_TYPE_TEXT -> {
                        lastMessageType.gone()
                        txtSubtitle.text = "$messagePrefix ${chatHeader.subtitle}"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_chat_video_2)
                        txtSubtitle.text = "$messagePrefix Video"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_chat_document_2)
                        txtSubtitle.text = "$messagePrefix Document"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_chat_image_2)
                        txtSubtitle.text = "$messagePrefix Image"
                    }
                    ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_chat_location_2)
                        txtSubtitle.text = "$messagePrefix Location"
                    }
                    else -> {
                        //   lastMessageType.gone()
                        txtSubtitle.text = ""
                    }
                }

                textViewTime.text = chatHeader.timeDisplay
                setReceivedStatus(chatHeader.status)
            }
        }
    }

    private fun setReceivedStatus(status: Int) = when (status) {
        ChatConstants.MESSAGE_STATUS_NOT_SENT -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_DELIVERED_TO_SERVER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_sent_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_delivered_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_READ_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_seen)
                    .into(statusIV)
        }
        else -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(statusIV)
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