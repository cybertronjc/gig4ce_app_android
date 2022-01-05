package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.net.VpnService.prepare
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

    var mediaPlayer: MediaPlayer? = null
    private var mExoPlayer: SimpleExoPlayer? = null
    var isPlaying: Boolean = false
    var fileToPlay: File? = null

    var currentlyPlayingId: String? = null
    var playOrPause: Boolean = false

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

        lifeCycleOwner?.let {
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE){
                oneToOneChatViewModel.enableSelect.observe(it, Observer {
                    it ?: return@Observer
                    if (it == false) {
                        frameLayoutRoot?.foreground = null
                    }
                })

//                oneToOneChatViewModel.currentlyPlayingAudioMessageId.observe(it, Observer {
//                    it ?: return@Observer
//
//                    if (currentlyPlayingId != it){
//                        Log.d("MyAudioComp", "pausing audio observer")
//                            playAudio.setImageDrawable(
//                                context?.resources?.getDrawable(
//                                    R.drawable.ic_play_audio_icon,
//                                    null
//                                )
//                            )
//                    }
//                })
//
            } else if(messageType == MessageType.GROUP_MESSAGE){
                groupChatViewModel.enableSelect.observe(it, Observer {
                    it ?: return@Observer
                    if (it == false) {
                        frameLayoutRoot?.foreground = null
                    }
                })
            }

        }

        playAudio.setOnClickListener {
            val file = returnFileIfAlreadyDownloadedElseNull()
//
            if (file != null) {
                navigation.navigateTo(
                    "chats/audioPlayer", bundleOf(
                        AudioPlayerBottomSheetFragment.INTENT_EXTRA_URI to file.path!!
                    )
                )
            } else{
                //download audio
                playProgress.visible()
                playAudio.gone()
                downloadAttachment()
            }
        }

//        playAudio.setOnClickListener {
//            //is audio playing already
//            val file = returnFileIfAlreadyDownloadedElseNull()
//
//            if (file != null) {
////                val uri = FileProvider.getUriForFile(
////                    context,
////                    context.packageName + ".provider",
////                    file.toFile()
////                )
//                val uri = Uri.parse(file.path)
//
//                val playingId = oneToOneChatViewModel.isAudioPlayingAlready() ?: ""
//                currentlyPlayingId = playingId
//                if (playingId.isNullOrBlank()) {
//                    Log.d("MyAudioComp", "playing id: $playingId")
//                    playAudio.setImageDrawable(
//                        context?.resources?.getDrawable(
//                            R.drawable.ic_baseline_pause_24,
//                            null
//                        )
//                    )
//                    isPlaying = true
//                    //play(uri)
//                    playPauseAudio(true, uri)
//                    oneToOneChatViewModel.playMyAudio(true, false, false, message.id,uri)
//                    //oneToOneChatViewModel.playMyAudio(false, message.id,uri)
//
//                } else if(playingId == message.id && isPlaying){
//                    Log.d("MyAudioComp", "pausing this audio id: $playingId")
//                    playAudio.setImageDrawable(
//                        context?.resources?.getDrawable(
//                            R.drawable.ic_play_audio_icon,
//                            null
//                        )
//                    )
//                    isPlaying = false
//                    //pause
//                    playPauseAudio(false, uri)
//                    oneToOneChatViewModel.playMyAudio(false, true, false, message.id,uri)
//
//                }
//                else if(playingId == message.id && !isPlaying){
//                    Log.d("MyAudioComp", " resuming audio id: $playingId")
//                    playAudio.setImageDrawable(
//                        context?.resources?.getDrawable(
//                            R.drawable.ic_baseline_pause_24,
//                            null
//                        )
//                    )
//                    isPlaying = true
//                    //play(uri)
//                    playPauseAudio(true, uri)
//                    oneToOneChatViewModel.playMyAudio(true, false, false, message.id,uri)
//
//                }else {
//                    Log.d("MyAudioComp", "playing diff audio id: $playingId")
//                    playAudio.setImageDrawable(
//                        context?.resources?.getDrawable(
//                            R.drawable.ic_baseline_pause_24,
//                            null
//                        )
//                    )
//                    isPlaying = true
//                    //play(uri)
//                    playPauseAudio(true, uri)
//                    oneToOneChatViewModel.playMyAudio(true, false, false, message.id,uri)
//                }
//            } else{
//                //download audio
//                playProgress.visible()
//                playAudio.gone()
//                downloadAttachment()
//            }
//
//        }

        textViewTime.text = msg.timestamp?.toDisplayText()
        //textView.text = msg.attachmentName

        when (msg.flowType) {
            "out" -> {
                setReceivedStatus(msg)
            }
        }
    }

    private fun handleAudioUploading() {

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

//    private fun playAudio(uri: Uri?){
//        if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
//
//            oneToOneChatViewModel.playMyAudio(
//                message.id,
//                uri = uri!!
//            )
//        } else if (messageType == MessageType.GROUP_MESSAGE) {
//            groupChatViewModel.deleteMessage(
//                message.id
//            )
//        }
//    }

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
        playAudio.gone()
    }

    private fun handleDownloadedCompleted() {
        Log.d("AudioView", "file downloaded")
        playProgress.gone()
        playAudio.visible()
        playAudio.performClick()
    }

    private fun extractMediaSourceFromUri(uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(context, "Exo")
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(uri)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(context, "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun playPauseAudio(play: Boolean, uri: Uri){
        val mediaSource = buildMediaSource(uri)
        val exoPlayer = ExoPlayerFactory.newSimpleInstance(
            context, DefaultRenderersFactory(context)
            , DefaultTrackSelector(),
            DefaultLoadControl()
        )
        exoPlayer.apply {
            // AudioAttributes here from exoplayer package !!!
            val attr = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(attr, true)
            prepare(mediaSource)
            // THAT IS ALL YOU NEED
            playWhenReady = true
//            if(play){
//                playWhenReady = true
//            } else {
//                playWhenReady = false
//            }
        }
    }

    private var mAttrs: AudioAttributes? = null

    private fun play(uri: Uri) {
        if (mExoPlayer == null) initializePlayer()
        val mediaSource = extractMediaSourceFromUri(uri)
        mExoPlayer?.apply {

            // AudioAttributes here from exoplayer package !!!
//            mAttrs?.let { initializeAttributes() }
             mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(mAttrs!!, true)
            prepare(mediaSource)
            play()
            Log.d("MyAudioComp", "playing audio")
        }
    }

    private fun play() {
        mExoPlayer?.apply {
            true.also { mExoPlayer?.playWhenReady = it }
            //updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            //mMediaSession?.isActive = true
        }
    }

    private fun initializePlayer() {
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(
            context, DefaultRenderersFactory(context)
            , DefaultTrackSelector(),
            DefaultLoadControl()
        )
    }

    private fun pause() {
        mExoPlayer?.apply {
            playWhenReady = false
//            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
//                //updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
//            }
        }
    }

    private fun stop() {
        // release the resources when the service is destroyed
        mExoPlayer?.playWhenReady = false
        mExoPlayer?.release()
        mExoPlayer = null
        //updatePlaybackState(PlaybackStateCompat.STATE_NONE)
//        mMediaSession?.isActive = false
//        mMediaSession?.release()
    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        stopSelf()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stop()
//    }

//    private fun updatePlaybackState(state: Int) {
//        // You need to change the state because the action taken in the controller depends on the state !!!
//        mMediaSession?.setPlaybackState(
//            PlaybackStateCompat.Builder().setState(
//                state // this state is handled in the media controller
//                , 0L
//                , 1.0f // Speed playing
//            ).build()
//        )
//    }

    private fun initializeAttributes() {
        mAttrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
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