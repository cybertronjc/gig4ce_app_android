package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.net.VpnService.prepare
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.screens.AudioPlayerBottomSheetFragment
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
abstract class AudioMessageView (
    private val flowType: MessageFlowType,
    private val messageType: MessageType,
    context: Context,
    attrs: AttributeSet?
) : MediaMessage(context, attrs),
    IViewHolder,
    View.OnClickListener,
    View.OnLongClickListener,
    PopupMenu.OnMenuItemClickListener{

    @Inject
    lateinit var navigation: INavigation

    //Views
    private lateinit var linearLayout: ConstraintLayout
    private lateinit var senderNameTV: TextView
//    private lateinit var textView: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: LinearLayout
    private lateinit var frameLayoutRoot: FrameLayout
    private lateinit var progressbar: View
    private lateinit var receivedStatusIV: ImageView
    private lateinit var playAudio: ImageView
    private lateinit var playProgress: View
    private lateinit var audioTimeText: TextView

    //Data
    private lateinit var chatMessage : ChatMessage

    init {
        setDefault()
        inflate()
        findViews()
        cardView.setOnClickListener(this)
        cardView.setOnLongClickListener(this)
    }

    private fun findViews() {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        linearLayout = this.findViewById(R.id.ll_msgContainer)
        //textView = this.findViewById(R.id.tv_file_name)
        frameLayoutRoot = this.findViewById(R.id.frame)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        progressbar = this.findViewById(R.id.progress)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        playAudio = this.findViewById(R.id.audio_type_iv)
        audioTimeText = this.findViewById(R.id.tv_audio_length)
        playProgress = this.findViewById(R.id.progress_bar)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        if (flowType == MessageFlowType.IN) {
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_audio_in, this, true)
        } else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_audio_out, this, true)
    }

    override fun onBind(msg: ChatMessage) {
        chatMessage = msg

        senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && flowType == MessageFlowType.IN
        senderNameTV.text = msg.senderInfo.name

        if (msg.attachmentPath.isNullOrBlank()) {
            handleAudioUploading()
        } else {
        }

        lifeCycleOwner?.let { it1 ->
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE){
                oneToOneChatViewModel.enableSelect.observe(it1, Observer {
                    it ?: return@Observer
                    if (it == false) {
                        frameLayoutRoot?.foreground = null
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
                groupChatViewModel.scrollToMessageId.observe(it1, Observer {
                    it ?: return@Observer
                    if (it == message.id){
                        blinkLayout()
                    }
                })
            }

        }

        playAudio.setOnClickListener {
            val file = returnFileIfAlreadyDownloadedElseNull()
            if (file != null) {
                navigation.navigateTo(
                    "chats/audioPlayer", bundleOf(
                        AudioPlayerBottomSheetFragment.INTENT_EXTRA_URI to file.path!!
                    )
                )
            } else{
                //download audio
                playProgress.visible()
                playAudio.invisible()
                downloadAttachment()
            }
        }

        textViewTime.text = msg.timestamp?.toDisplayText()
        //textView.text = msg.attachmentName

        when (msg.flowType) {
            "out" -> {
                setReceivedStatus(msg)
            }
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

    private fun handleAudioUploading() {
        playProgress.invisible()
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
        if(!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                oneToOneChatViewModel.makeSelectEnable(true)
                oneToOneChatViewModel.selectChatMessage(message)
            } else if (messageType == MessageType.GROUP_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                groupChatViewModel.makeSelectEnable(true)
                groupChatViewModel.selectChatMessage(message)
            }
        }


        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> {}
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
            R.id.action_save_to_gallery -> saveDocumentToDownloads(
                returnFileIfAlreadyDownloadedElseNull()
            )
        }
        return true
    }

    private fun saveDocumentToDownloads(
        uri : Uri?
    ){
        uri ?: return
        GlobalScope.launch {

            try {
                MediaStoreApiHelpers.saveDocumentToDownloads(context,uri)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Document saved in downloads", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Unable to save document in downloads", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun viewMessageInfo() {
        navigation.navigateTo("chats/messageInfo",
            bundleOf(
                GroupMessageViewInfoFragment.INTENT_EXTRA_GROUP_ID to chatMessage.groupId,
                GroupMessageViewInfoFragment.INTENT_EXTRA_MESSAGE_ID to chatMessage.id
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
        return message
    }

    override fun onClick(v: View?) {
        val file = returnFileIfAlreadyDownloadedElseNull()

        if (file == null) {
            downloadAttachment()
        }
    }


    private fun downloadAttachment() = GlobalScope.launch {

        this.launch(Dispatchers.Main) {
            handleDownloadInProgress()
        }

        try {
            downloadMediaFile()
            this.launch(Dispatchers.Main) {
                handleDownloadedCompleted()
            }
        } catch (e: Exception) {
        }
    }

    private fun handleDownloadInProgress() {
        Log.d("AudioView", "downloading...")
        playProgress.visible()
        playAudio.invisible()
    }

    private fun handleDownloadedCompleted() {
        Log.d("AudioView", "file downloaded")
        playProgress.gone()
        playAudio.visible()
        playAudio.performClick()
    }
}

class InOneToOneAudioMessageView(
    context: Context,
    attrs: AttributeSet?
) : AudioMessageView(
    flowType = MessageFlowType.IN,
    messageType = MessageType.ONE_TO_ONE_MESSAGE,
    context = context,
    attrs = attrs
)

class OutOneToOneAudioMessageView(
    context: Context,
    attrs: AttributeSet?
) : AudioMessageView(
    flowType = MessageFlowType.OUT,
    messageType = MessageType.ONE_TO_ONE_MESSAGE,
    context = context,
    attrs = attrs
)

class GroupInAudioMessageView(
    context: Context,
    attrs: AttributeSet?
) : AudioMessageView(
    flowType = MessageFlowType.IN,
    messageType = MessageType.GROUP_MESSAGE,
    context = context,
    attrs = attrs
)

class GroupOutAudioMessageView(
    context: Context,
    attrs: AttributeSet?
) : AudioMessageView(
    flowType = MessageFlowType.OUT,
    messageType = MessageType.GROUP_MESSAGE,
    context = context,
    attrs = attrs
)