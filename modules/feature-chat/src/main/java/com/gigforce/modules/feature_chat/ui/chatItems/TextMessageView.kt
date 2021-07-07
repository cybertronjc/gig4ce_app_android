package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.DisplayUtil
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
abstract class TextMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder,
        View.OnLongClickListener,
        PopupMenu.OnMenuItemClickListener,
        BaseChatMessageItemView{

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private lateinit var containerView: View
    private lateinit var senderNameTV: TextView
    private lateinit var msgView: TextView
    private lateinit var timeView: TextView
    private lateinit var receivedStatusIV: ImageView
    private lateinit var quotedMessagePreviewContainer : LinearLayout

    private lateinit var message: ChatMessage
    private lateinit var oneToOneChatViewModel: ChatPageViewModel
    private lateinit var groupChatViewModel: GroupChatViewModel

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val view = if (type == MessageFlowType.IN)
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_out, this, true)
        loadViews(view)

    }

    fun loadViews(
            view: View
    ) {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        msgView = this.findViewById(R.id.tv_msgValue)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)

        quotedMessagePreviewContainer = findViewById(R.id.reply_messages_quote_container_layout)
        containerView = this.findViewById(R.id.ll_msgContainer)
        containerView.setOnLongClickListener(this)

        val screenWidth = DisplayUtil.getScreenWidthInPx(context)
        val maxWidth = (screenWidth * 0.70).toInt()
        msgView.maxWidth = maxWidth
    }

    override fun bind(data: Any?) {
        data?.let {
            val dataAndViewModels = it as ChatMessageWrapper
            message = dataAndViewModels.message
            groupChatViewModel = dataAndViewModels.groupChatViewModel
            oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

            senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = message.senderInfo.name

            setQuotedMessageOnView(message)

            if (message.mentionedUsersInfo.isNotEmpty()) {
                val incrementingMentions = message.mentionedUsersInfo.sortedBy { it.startFrom }
                val spannableString = SpannableStringBuilder(message.content)

                for (i in incrementingMentions.indices) {

                    val mention = incrementingMentions[i]
                    spannableString.setSpan(
                            PositionClickableSpan(i),
                            mention.startFrom,
                            mention.endTo,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }

                msgView.setText(spannableString)
                msgView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                msgView.setText(message.content)
            }
            LinkifyCompat.addLinks(msgView, Linkify.ALL)

            timeView.setText(message.timestamp?.toDisplayText())
            setReceivedStatus(message)
        }
    }

    private fun setQuotedMessageOnView(
        chatMessage: ChatMessage
    ) {
        if(message.isAReplyToOtherMessage){
            quotedMessagePreviewContainer.removeAllViews()

            val replyView = LayoutInflater.from(context).inflate(
                R.layout.layout_reply_to_layout,
                null,
                false
            )
            quotedMessagePreviewContainer.addView(replyView)

            //Setting common vars and listeners
            val senderNameTV: TextView = replyView.findViewById(R.id.user_name_tv)
            val messageTV: TextView = replyView.findViewById(R.id.tv_msgValue)
            val messageImageIV: GigforceImageView = replyView.findViewById(R.id.message_image)

            senderNameTV.text = chatMessage.senderInfo.name

            when (chatMessage.type) {
                com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT -> {
                    messageTV.text = chatMessage.content
                    messageImageIV.gone()
                }
                com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else if(chatMessage.attachmentPath != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                    } else {
                        //load default image
                    }
                }
                com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else {
                        //load default image
                    }
                }
                com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                    messageTV.text = chatMessage.locationPhysicalAddress
                    messageImageIV.visible()

                    if(chatMessage.thumbnailBitmap != null){
                        messageImageIV.loadImage(chatMessage.thumbnailBitmap!!,true)
                    } else if(chatMessage.thumbnail != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.thumbnail!!)
                    }else if(chatMessage.attachmentPath != null){
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(chatMessage.attachmentPath!!)
                    } else {
                        //load default image
                    }
                }
                com.gigforce.common_ui.core.ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                    messageTV.text = chatMessage.attachmentName
                    messageImageIV.visible()
                    messageImageIV.loadImage(R.drawable.ic_document_background)
                }
                else -> {
                }
            }

        } else{
            quotedMessagePreviewContainer.removeAllViews()
        }
    }

    private fun setReceivedStatus(msg: ChatMessage) = when (msg.status) {
        ChatConstants.MESSAGE_STATUS_NOT_SENT -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_DELIVERED_TO_SERVER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_sent)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_delivered)
                    .into(receivedStatusIV)
        }
        ChatConstants.MESSAGE_STATUS_READ_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_seen)
                    .into(receivedStatusIV)
        }
        else -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
        }
    }

    override fun onLongClick(v: View?): Boolean {

        val popUpMenu = PopupMenu(context, v)
        popUpMenu.inflate(R.menu.menu_chat_clipboard)

        popUpMenu.menu.findItem(R.id.action_copy).isVisible = true
        popUpMenu.menu.findItem(R.id.action_delete).isVisible = type == MessageFlowType.OUT
        popUpMenu.menu.findItem(R.id.action_message_info).isVisible =
                type == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE

        popUpMenu.setOnMenuItemClickListener(this)
        popUpMenu.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> copyMessageToClipBoard()
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
        }
        return true
    }

    private fun viewMessageInfo() {
        navigation.navigateTo(
                "chats/messageInfo",
                bundleOf(
                        GroupMessageViewInfoFragment.INTENT_EXTRA_GROUP_ID to message.groupId,
                        GroupMessageViewInfoFragment.INTENT_EXTRA_MESSAGE_ID to message.id
                )
        )
    }

    private fun deleteMessage() {
        if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {

            oneToOneChatViewModel.deleteMessage(
                    message.id
            )
        } else if (messageType == MessageType.GROUP_MESSAGE) {

            groupChatViewModel.deleteMessage(
                    message.id
            )
        }
    }

    private fun copyMessageToClipBoard() {
        val clip: ClipData = ClipData.newPlainText("Copy", msgView.text)
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
                clip
        )
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    private inner class PositionClickableSpan constructor(
            private val position: Int
    ) : ClickableSpan() {

        override fun onClick(widget: View) {
            val mentionsInMesssage = message.mentionedUsersInfo
            if (position == -1 || position > mentionsInMesssage.size - 1)
                return

            val mention = mentionsInMesssage[position]
            chatNavigation.navigateToChatPage(
                    chatType = ChatConstants.CHAT_TYPE_USER,
                    otherUserId = mention.userMentionedUid,
                    otherUserName = mention.profileName,
                    otherUserProfilePicture = mention.profilePicture,
                    sharedFileBundle = null,
                    headerId = "",
                    cameFromLinkInOtherChat = true
            )
        }
    }

    override fun getCurrentChatMessageOrThrow(): ChatMessage {
        return message
    }
}

class InTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.IN,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class OutTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.OUT,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class GroupInTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.IN,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)

class GroupOutTextMessageView(
        context: Context,
        attrs: AttributeSet?
) : TextMessageView(
        MessageFlowType.OUT,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)