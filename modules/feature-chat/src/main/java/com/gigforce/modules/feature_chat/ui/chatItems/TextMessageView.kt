package com.gigforce.modules.feature_chat.ui.chatItems

import android.animation.ArgbEvaluator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_ui.DisplayUtil
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.view.MotionEvent
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.animation.LinearInterpolator

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.animation.ValueAnimator

import android.animation.ValueAnimator.AnimatorUpdateListener
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.chat.models.MentionUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch




@AndroidEntryPoint
abstract class TextMessageView(
    val type: MessageFlowType,
    val messageType: MessageType,
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs),
    IViewHolder,
    View.OnClickListener,
    View.OnLongClickListener,
    PopupMenu.OnMenuItemClickListener,
    BaseChatMessageItemView {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val firebaseAuthStateListener: FirebaseAuthStateListener by lazy {
        FirebaseAuthStateListener.getInstance()
    }

    private lateinit var containerView: View
    private lateinit var senderNameTV: TextView
    private lateinit var msgView: TextView
    private lateinit var timeView: TextView
    private lateinit var receivedStatusIV: ImageView
    private lateinit var quotedMessagePreviewContainer: LinearLayout

    private lateinit var message: ChatMessage
    private lateinit var oneToOneChatViewModel: ChatPageViewModel
    private lateinit var groupChatViewModel: GroupChatViewModel
    private lateinit var frameLayoutRoot: FrameLayout
    private var selectedMessageList = emptyList<ChatMessage>()

    init {
        setDefault()
        inflate()
        setListeners()
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

    private fun setListeners() {
        senderNameTV.setOnClickListener(this)
        msgView.setOnLongClickListener(OnLongClickListener {
            containerView.performLongClick()
            true
        })

//        msgView.setOnTouchListener(OnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                Log.d("TextMessageView", "long click on text motion")
//                frameLayoutRoot.performLongClick()
//                return@OnTouchListener true
//            }
//            if (event.action == MotionEvent.ACTION_DOWN) {
//            }
//            v.onTouchEvent(event)
//        })
    }

    fun loadViews(
        view: View
    ) {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        msgView = this.findViewById(R.id.tv_msgValue)
        timeView = this.findViewById(R.id.tv_msgTimeValue)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        frameLayoutRoot = this.findViewById(R.id.frame)
        quotedMessagePreviewContainer =
            this.findViewById(R.id.reply_messages_quote_container_layout)
        containerView = this.findViewById(R.id.ll_msgContainer)

        val screenWidth = DisplayUtil.getScreenWidthInPx(context)
        val maxWidth = (screenWidth * 0.70).toInt()
        msgView.maxWidth = maxWidth

        quotedMessagePreviewContainer.setOnClickListener(this)
        msgView.setOnClickListener(this)
        containerView.setOnClickListener(this)
        containerView.setOnLongClickListener(this)
    }

    override fun bind(data: Any?) {
        data?.let { it ->
            val dataAndViewModels = it as ChatMessageWrapper
            message = dataAndViewModels.message
            groupChatViewModel = dataAndViewModels.groupChatViewModel
            oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

            senderNameTV.isVisible =
                messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = message.senderInfo.name

            dataAndViewModels.lifeCycleOwner?.let { it1 ->
                if (messageType == MessageType.ONE_TO_ONE_MESSAGE){
                    oneToOneChatViewModel.enableSelect.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == false) {
                            frameLayoutRoot.foreground = null
                        }
                    })
                    oneToOneChatViewModel.selectedChatMessage.observe(it1, Observer {
                        it ?: return@Observer
                        selectedMessageList = it
                        if (it.isNotEmpty() && it.contains(message)){
                            Log.d("MultiSelection", "Contains this message $it")
                            frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        } else {
                            frameLayoutRoot.foreground = null
                        }

                    })
                    oneToOneChatViewModel.scrollToMessageId.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == message.id){
                            blinkLayout()
                        }
                    })
                } else if(messageType == MessageType.GROUP_MESSAGE){
                    groupChatViewModel.enableSelect.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == false) {
                            frameLayoutRoot.foreground = null
                        }
                    })
                    groupChatViewModel.selectedChatMessage.observe(it1, Observer {
                        it ?: return@Observer
                        selectedMessageList = it
                        if (it.isNotEmpty() && it.contains(message)){
                            Log.d("MultiSelection", "Contains this message $it")
                            frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        } else {
                            frameLayoutRoot.foreground = null
                        }

                    })
                    groupChatViewModel.scrollToMessageId.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == message.id){
                            blinkLayout()
                        }
                    })
                }

            }


            setQuotedMessageOnView(
                context =  context,
                firebaseAuthStateListener = firebaseAuthStateListener,
                type = type,
                chatMessage = message,
                quotedMessagePreviewContainer = quotedMessagePreviewContainer
            )

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

//            if (message.mentionedUsersInfo.isNotEmpty()) {
//
//                groupChatViewModel.viewModelScope.launch {
//                    val incrementingMentions = message.mentionedUsersInfo.sortedBy { it.startFrom }
//                    var msgContent = message.content
//                    var spannableString = SpannableStringBuilder("")
//                for (i in incrementingMentions.indices) {
//                    val it = incrementingMentions[i]
//                    var storedContactName = ""
//                        storedContactName = groupChatViewModel.getContactStoredByMobile(it.userMentionedUid)
//                        Log.d("TextMessageView", "$msgContent , name: $storedContactName")
//                        if (storedContactName.isNotEmpty()){
//                            //spannableString = spannableString.replace(msgContent.indexOf(storedContactName) + 1, msgContent.indexOf(storedContactName) + storedContactName.length , storedContactName)
//                            msgContent = msgContent.replace(it.profileName, storedContactName, true)
//                            spannableString = SpannableStringBuilder(msgContent)
//                            spannableString.setSpan(
//                                PositionClickableSpan(i),
//                                msgContent.indexOf(storedContactName),
//                                msgContent.indexOf(storedContactName) + storedContactName.length,
//                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                            )
//                        } else {
//                            spannableString = SpannableStringBuilder(msgContent)
//                            spannableString.setSpan(
//                                PositionClickableSpan(i),
//                                it.startFrom,
//                                it.endTo,
//                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                            )
//                        }
//                        Log.d("TextMessageView1", "content: $msgContent , spannable: $spannableString ")
//                    }
//                    Log.d("TextMessageView2", "content: $msgContent , spannable: $spannableString ")
//                    msgView.text = spannableString
//                    msgView.movementMethod = LinkMovementMethod.getInstance()
//                }
//
//            } else {
//                msgView.text = message.content
//            }
            LinkifyCompat.addLinks(msgView, Linkify.ALL)

            timeView.setText(message.timestamp?.toDisplayText())
            setReceivedStatus(message)
        }
    }

    private fun blinkLayout(){
        frameLayoutRoot.background = resources.getDrawable(R.drawable.selected_chat_foreground)
        Handler(Looper.getMainLooper()).postDelayed({
                 frameLayoutRoot.background = null
                 if (messageType == MessageType.GROUP_MESSAGE){
                     groupChatViewModel.setScrollToMessageNull()
                 } else {
                     oneToOneChatViewModel.setScrollToMessageNull()
                 }

        },2000)
    }

    private fun setReceivedStatus(msg: ChatMessage) {

            when (msg.status) {
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
    }


    override fun onLongClick(v: View?): Boolean {

//        val popUpMenu = PopupMenu(context, v)
//        popUpMenu.inflate(R.menu.menu_chat_clipboard)
//
//        popUpMenu.menu.findItem(R.id.action_save_to_gallery).isVisible = false
//        popUpMenu.menu.findItem(R.id.action_copy).isVisible = true
//        popUpMenu.menu.findItem(R.id.action_delete).isVisible = type == MessageFlowType.OUT
//        popUpMenu.menu.findItem(R.id.action_message_info).isVisible =
//            type == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE
//
//        popUpMenu.setOnMenuItemClickListener(this)
//        popUpMenu.show()

        if(!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)){
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                frameLayoutRoot?.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                oneToOneChatViewModel.makeSelectEnable(true)
                oneToOneChatViewModel.selectChatMessage(message, true)
            } else if (messageType == MessageType.GROUP_MESSAGE) {
                frameLayoutRoot?.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                groupChatViewModel.makeSelectEnable(true)
                groupChatViewModel.selectChatMessage(message, true)
            }
        }

        return true
    }

    override fun onClick(v: View?) {

        if (v?.id == R.id.ll_msgContainer || v?.id == R.id.tv_msgValue){
            if((oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
                if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                    if (selectedMessageList.contains(message)){
                        //remove
                        frameLayoutRoot.foreground = null
                        oneToOneChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        oneToOneChatViewModel.selectChatMessage(message, true)
                    }
                } else if (messageType == MessageType.GROUP_MESSAGE) {
                    if (selectedMessageList.contains(message)){
                        //remove
                        frameLayoutRoot.foreground = null
                        groupChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        groupChatViewModel.selectChatMessage(message, true)
                    }

                }
            }
        } else if (v?.id == R.id.reply_messages_quote_container_layout){
            val replyMessage = message.replyForMessage ?: return
            if(!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {

                if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                    oneToOneChatViewModel.scrollToMessage(
                        replyMessage
                    )
                } else if (messageType == MessageType.GROUP_MESSAGE) {
                    Log.d("replyToMessage", "scrolling")
                    groupChatViewModel.scrollToMessage(
                        replyMessage
                    )
                }
            }
        } else if (v?.id == R.id.user_name_tv){
            //navigate to chat page
            navigation.popBackStack()
            chatNavigation.navigateToChatPage(
                chatType = ChatConstants.CHAT_TYPE_USER,
                otherUserId = message.senderInfo.id,
                otherUserName = message.senderInfo.name,
                otherUserProfilePicture = message.senderInfo.profilePic,
                sharedFileBundle = null,
                headerId = "",
                cameFromLinkInOtherChat = true
            )

        }

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