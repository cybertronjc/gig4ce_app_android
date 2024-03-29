
package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
abstract class VideoMessageView(
    val type: MessageFlowType,
    val messageType: MessageType,
    context: Context,
    attrs: AttributeSet?
) : MediaMessage(
    context,
    attrs
), View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    //View
    private lateinit var senderNameTV: TextView
    private lateinit var imageView: GigforceImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: LinearLayout
    private lateinit var frameLayoutRoot: FrameLayout
    private lateinit var attachmentNameTV: TextView
    private lateinit var playDownloadIconIV: ImageView
    private lateinit var playDownloadOverlayIV: ImageView
    private lateinit var attachmentUploadingDownloadingProgressBar: ProgressBar
    private lateinit var videoLength: TextView
    private lateinit var linearRoot: LinearLayout
    private lateinit var receivedStatusIV: ImageView
    private lateinit var chatMessageText: TextView

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private var chatMessage : ChatMessage? = null

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private var selectedMessageList = emptyList<ChatMessage>()

    init {
        setDefault()
        inflate()
        setOnClickListeners()
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        if (type == MessageFlowType.IN)
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_chat_text_with_video_in, this, true)
        else
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_chat_text_with_video_out, this, true)

        loadViews()
    }

    private fun setOnClickListeners() {
        cardView.setOnClickListener(this)
        cardView.setOnLongClickListener(this)
        senderNameTV.setOnClickListener(this)
        linearRoot.setOnClickListener(this)
        linearRoot.setOnLongClickListener(this)
    }

    fun loadViews() {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        imageView = this.findViewById(R.id.iv_image)
        frameLayoutRoot = this.findViewById(R.id.frame)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        linearRoot = this.findViewById(R.id.linearRoot)
        attachmentNameTV = this.findViewById(R.id.tv_file_name)
        playDownloadIconIV = this.findViewById(R.id.play_download_icon_iv)
        playDownloadOverlayIV = this.findViewById(R.id.play_download_overlay_iv)
        attachmentUploadingDownloadingProgressBar =
            this.findViewById(R.id.attachment_downloading_pb)
        videoLength = this.findViewById(R.id.video_length_tv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        chatMessageText = this.findViewById(R.id.chat_text)
    }

    override fun onBind(msg: ChatMessage) {
        chatMessage = msg

        attachmentNameTV.text = msg.attachmentName
        videoLength.text = convertMicroSecondsToNormalFormat(msg.videoLength)
        textViewTime.text = msg.timestamp?.toDisplayText()

        senderNameTV.isVisible =
            messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
        senderNameTV.text = msg.senderInfo.name

        chatMessageText.text = msg.content.toString() ?: ""
        loadThumbnail(msg)

        lifeCycleOwner?.let { it1 ->
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE){
                oneToOneChatViewModel.enableSelect.observe(it1, Observer {
                    it ?: return@Observer
                    if (it == false) {
                        frameLayoutRoot?.foreground = null
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
                        frameLayoutRoot?.foreground = null
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


        when (msg.flowType) {
            "in" -> {
                receivedStatusIV.gone()
            }
            "out" -> {
                receivedStatusIV.visible()
                setReceivedStatus(msg)
            }
        }

        if (msg.thumbnailBitmap != null) {
            handleVideoUploading()
        } else {
            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            if (downloadedFile != null) {
                handleVideoDownloaded()
            } else {
                handleVideoNotDownloaded()
            }
        }
    }

    private fun loadThumbnail(msg: ChatMessage) {
        if (msg.thumbnailBitmap != null) {
            imageView.loadImage(msg.thumbnailBitmap!!,true)
        } else if (msg.thumbnail != null) {

            imageView.loadImageIfUrlElseTryFirebaseStorage(
                    msg.thumbnail!!,
            -1,
                    -1,
                    true
                    )
        }
    }

    private fun blinkLayout(){
        frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
        Handler(Looper.getMainLooper()).postDelayed({
            frameLayoutRoot.foreground = null
            if (messageType == MessageType.GROUP_MESSAGE){
                groupChatViewModel.setScrollToMessageNull()
            } else {
                oneToOneChatViewModel.setScrollToMessageNull()
            }
        },2000)
    }


    private fun setReceivedStatus(msg: ChatMessage)  {
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

    private fun handleVideoDownloaded() {

        attachmentUploadingDownloadingProgressBar.gone()
        playDownloadOverlayIV.visible()
        playDownloadIconIV.visible()
        Glide.with(context).load(R.drawable.ic_play_2).into(playDownloadIconIV)
    }

    private fun handleDownloadInProgress() {
        playDownloadOverlayIV.visible()
        playDownloadIconIV.gone()
        attachmentUploadingDownloadingProgressBar.visible()
    }

    private fun handleVideoNotDownloaded() {
        attachmentUploadingDownloadingProgressBar.gone()
        playDownloadOverlayIV.visible()
        playDownloadIconIV.visible()
        Glide.with(context).load(R.drawable.ic_download_24).into(playDownloadIconIV)
    }

    private fun handleVideoUploading() {
        playDownloadOverlayIV.visible()
        playDownloadIconIV.gone()
        attachmentUploadingDownloadingProgressBar.visible()

    }

    private fun convertMicroSecondsToNormalFormat(videoAttachmentLength: Long): String {
        if (videoAttachmentLength == 0L)
            return ""

        if (videoAttachmentLength > 3600000) {
            return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(videoAttachmentLength),
                TimeUnit.MILLISECONDS.toMinutes(videoAttachmentLength) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(videoAttachmentLength)
                ),
                TimeUnit.MILLISECONDS.toSeconds(videoAttachmentLength) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                videoAttachmentLength
                            )
                        )
            )
        } else {
            return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(videoAttachmentLength) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(videoAttachmentLength)
                ),
                TimeUnit.MILLISECONDS.toSeconds(videoAttachmentLength) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                videoAttachmentLength
                            )
                        )
            )
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.cv_msgContainer || v?.id == R.id.linearRoot) {
            if ((oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
                if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                    if (selectedMessageList.contains(message)) {
                        //remove
                        frameLayoutRoot.foreground = null
                        oneToOneChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground =
                            resources.getDrawable(R.drawable.selected_chat_foreground)
                        oneToOneChatViewModel.selectChatMessage(message, true)
                    }
                } else if (messageType == MessageType.GROUP_MESSAGE) {
                    if (selectedMessageList.contains(message)) {
                        //remove
                        frameLayoutRoot.foreground = null
                        groupChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground =
                            resources.getDrawable(R.drawable.selected_chat_foreground)
                        groupChatViewModel.selectChatMessage(message, true)
                    }

                }
            } else {
                val file = returnFileIfAlreadyDownloadedElseNull()

                if (file != null) {

                    chatNavigation.openFullScreenVideoDialogFragment(file)
                } else {
                    downloadAttachment()
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

    private fun downloadAttachment() = GlobalScope.launch {

        this.launch(Dispatchers.Main) {
            handleDownloadInProgress()
        }

        try {
            downloadMediaFile()
            this.launch(Dispatchers.Main) {
                handleVideoDownloaded()
            }
        } catch (e: Exception) {
        }
    }

    override fun onLongClick(v: View?): Boolean {
//        val popUpMenu = PopupMenu(context, v)
//        popUpMenu.inflate(R.menu.menu_chat_clipboard)
//
//        popUpMenu.menu.findItem(R.id.action_save_to_gallery).isVisible = returnFileIfAlreadyDownloadedElseNull() != null
//        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
//        popUpMenu.menu.findItem(R.id.action_delete).isVisible = type == MessageFlowType.OUT
//        popUpMenu.menu.findItem(R.id.action_message_info).isVisible = type == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE
//
//        popUpMenu.setOnMenuItemClickListener(this)
//        popUpMenu.show()
        if(!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                oneToOneChatViewModel.makeSelectEnable(true)
                oneToOneChatViewModel.selectChatMessage(message, true)
            } else if (messageType == MessageType.GROUP_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                groupChatViewModel.makeSelectEnable(true)
                groupChatViewModel.selectChatMessage(message, true)
            }
        }

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_save_to_gallery -> saveToGallery(
                returnFileIfAlreadyDownloadedElseNull()
            )
            R.id.action_copy -> { }
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
        }
        return true
    }

    private fun saveToGallery(
        videoUri: Uri?
    ) {
        val uri = videoUri ?: return
        GlobalScope.launch {
            try {
                MediaStoreApiHelpers.saveVideoToGallery(
                    context,
                    uri
                )

                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Video saved to gallery", Toast.LENGTH_SHORT).show()
                    var map = mapOf("media_type" to "Video")
                    eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_MEDIA_SAVED_TO_GALLERY, map))
                }
            } catch (e: Exception) {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Unable to save video to gallery", Toast.LENGTH_SHORT).show()
                    var map = mapOf("media_type" to "Video")
                    eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_MEDIA_FAILED_TO_SAVE, map))
                }
            }
        }
    }

    private fun viewMessageInfo() {
        navigation.navigateTo("chats/messageInfo",
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

    override fun getCurrentChatMessageOrThrow(): ChatMessage {
        return chatMessage ?: throw IllegalStateException("chat message is null")
    }
}

class InVideoMessageView(
    context: Context,
    attrs: AttributeSet?
) : VideoMessageView(
    MessageFlowType.IN,
    MessageType.ONE_TO_ONE_MESSAGE,
    context,
    attrs
)

class OutVideoMessageView(
    context: Context,
    attrs: AttributeSet?
) : VideoMessageView(
    MessageFlowType.OUT,
    MessageType.ONE_TO_ONE_MESSAGE,
    context,
    attrs
)

class GroupInVideoMessageView(
    context: Context,
    attrs: AttributeSet?
) : VideoMessageView(
    MessageFlowType.IN,
    MessageType.GROUP_MESSAGE,
    context,
    attrs
)

class GroupOutVideoMessageView(
    context: Context,
    attrs: AttributeSet?
) : VideoMessageView(
    MessageFlowType.OUT,
    MessageType.GROUP_MESSAGE,
    context,
    attrs
)