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
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
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
    private lateinit var audioTimeText: TextView

    var mediaPlayer: MediaPlayer? = null
    private var mExoPlayer: SimpleExoPlayer? = null
    var isPlaying: Boolean = false
    var fileToPlay: File? = null

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
            //is audio playing already
            var messageIdPlaying = ""
            if(messageType == MessageType.ONE_TO_ONE_MESSAGE){
                 messageIdPlaying = oneToOneChatViewModel.isAudioPlayingAlready()
            } else if (messageType == MessageType.GROUP_MESSAGE){
                messageIdPlaying = oneToOneChatViewModel.isAudioPlayingAlready()
            }

            if (isPlaying){
                Log.d("audio", "was already playing this audio , id: $messageIdPlaying , isPlaying: $isPlaying")
                playAudio.setImageDrawable(
                    context?.resources?.getDrawable(
                        R.drawable.ic_play_audio_icon,
                        null
                    )
                )
                //pause
                isPlaying = false
                stop()
            } else{
                Log.d("audio", "was not already playing this audio so play my audio , id: $messageIdPlaying , isPlaying: $isPlaying")
                val file = returnFileIfAlreadyDownloadedElseNull()

                if (file != null){
                    //play the audio
                    Log.d("audio", "${file.path}")
                    val uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        file.toFile()
                    )
                    playAudio.setImageDrawable(
                        context?.resources?.getDrawable(
                            R.drawable.ic_baseline_pause_24,
                            null
                        )
                    )
                    isPlaying = false
                    playMyAudio(uri)
                } else {
                    //download first
                    progressbar.visible()
                    downloadAttachment()
                    playAudio.setImageDrawable(
                        context?.resources?.getDrawable(
                            R.drawable.ic_play_audio_icon,
                            null
                        )
                    )
                }
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
//        val popUpMenu = PopupMenu(context, v)
//        popUpMenu.inflate(R.menu.menu_chat_clipboard)
//
//        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
//        popUpMenu.menu.findItem(R.id.action_save_to_gallery).title = "Save to downloads"
//        popUpMenu.menu.findItem(R.id.action_save_to_gallery).isVisible = returnFileIfAlreadyDownloadedElseNull() != null
//        popUpMenu.menu.findItem(R.id.action_delete).isVisible =  flowType == MessageFlowType.OUT
//        popUpMenu.menu.findItem(R.id.action_message_info).isVisible =  flowType == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE
//
//        popUpMenu.setOnMenuItemClickListener(this)
//        popUpMenu.show()
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

    private fun playAudio(uri: Uri?){
        if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {

            oneToOneChatViewModel.playMyAudio(
                message.id,
                uri = uri!!
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
            //openDocument(file)
            //play audio
//            if (isPlaying) {
//                stopAudio()
//                playAudio(file)
//            } else {
//                playAudio(file)
//            }
//            Log.d("AudioView", "Playing audio")
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
        progressbar.visible()
    }

    private fun handleDownloadedCompleted() {
        Log.d("AudioView", "file downloaded")
        progressbar.gone()
    }

    private fun extractMediaSourceFromUri(uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(context, "Exo")
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(uri)
    }

    private fun playMyAudio(uri: Uri){
        val mediaSource = extractMediaSourceFromUri(uri)
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
        }
    }

    private var mAttrs: AudioAttributes? = null

    private fun play(mediaSource: MediaSource) {
        if (mExoPlayer == null) initializePlayer()
        mExoPlayer?.apply {

            // AudioAttributes here from exoplayer package !!!
            mAttrs?.let { initializeAttributes() }
            // In 2.9.X you don't need to manually handle audio focus :D
            setAudioAttributes(mAttrs!!, true)
            prepare(mediaSource)
            play()
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


//    private fun playAudio(file: Uri?) {
//        mediaPlayer = MediaPlayer()
//
//        try {
//            mediaPlayer!!.setDataSource(file!!.path)
//            mediaPlayer!!.prepare()
//            mediaPlayer!!.start()
//            Log.d("AudioView", "audio started path: $file!!.path")
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.d("AudioView", "error playing audio ${e.message} , ")
//        }
//        playAudio.setImageDrawable(
//            context?.resources?.getDrawable(
//                R.drawable.ic_baseline_pause_24,
//                null
//            )
//        )
////        media_file_name.setText(fileToPlay?.name)
////        player_header_status.setText("Playing")
//        isPlaying = true
//
//        mediaPlayer!!.setOnCompletionListener {
//            it.release()
//            stopAudio()
//            Log.d("AudioView", "completed listener")
////            player_header_status.setText("Finished")
//        }
////        seekBar.max = mediaPlayer!!.duration
////        seekBarHandler = Handler()
//        //updateRunnable()
//
////        seekBarHandler!!.postDelayed(updateSeekBar!!, 0)
//
//    }

//    private fun updateRunnable() {
//        updateSeekBar = object : Runnable {
//            override fun run() {
//                seekBar.progress = mediaPlayer!!.currentPosition
//                seekBarHandler!!.postDelayed(this, 500)
//            }
//        }
//    }

    private fun pauseAudio() {
        playAudio.setImageDrawable(
            context?.resources?.getDrawable(
                R.drawable.ic_play_audio_icon,
                null
            )
        )
        mediaPlayer!!.pause()
        isPlaying = false
        //seekBarHandler!!.removeCallbacks(updateSeekBar!!)
    }

    private fun resumeAudio() {
        playAudio.setImageDrawable(
            context?.resources?.getDrawable(
                R.drawable.ic_baseline_pause_24,
                null
            )
        )
        mediaPlayer!!.start()
        isPlaying = true
//        updateRunnable()
//        seekBarHandler!!.postDelayed(updateSeekBar!!, 0)
    }

    private fun stopAudio() {
        Log.d("AudioView", "completed stopping audio")
        playAudio.setImageDrawable(
            context?.resources?.getDrawable(
                R.drawable.ic_play_audio_icon,
                null
            )
        )
        //player_header_status.setText("Stopped")
        isPlaying = false
        mediaPlayer!!.stop()
        //seekBarHandler!!.removeCallbacks(updateSeekBar!!)
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