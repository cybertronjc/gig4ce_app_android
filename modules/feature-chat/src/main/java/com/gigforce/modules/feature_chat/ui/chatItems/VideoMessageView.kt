package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class VideoMessageView(
    val type: String,
    context: Context,
    attrs: AttributeSet?
) : MediaMessage(
    context,
    attrs
), View.OnClickListener {

    //View
    private lateinit var imageView: ImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var attachmentNameTV: TextView
    private lateinit var playDownloadIconIV: ImageView
    private lateinit var playDownloadOverlayIV: ImageView
    private lateinit var attachmentUploadingDownloadingProgressBar: ProgressBar
    private lateinit var videoLength: TextView
    private lateinit var receivedStatusIV: ImageView

    @Inject
    lateinit var navigation: IChatNavigation

    init {
        setDefault()
        inflate()
        setOnClickListeners()

        (this.context.applicationContext as ChatModuleProvider)
            .provideChatModule()
            .inject(this)
        navigation.context = context
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        if (type == "in")
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_video_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_video_out, this, true)
        loadViews()
    }

    private fun setOnClickListeners() {
        cardView.setOnClickListener(this)
    }

    fun loadViews() {
        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        attachmentNameTV = this.findViewById(R.id.tv_file_name)
        playDownloadIconIV = this.findViewById(R.id.play_download_icon_iv)
        playDownloadOverlayIV = this.findViewById(R.id.play_download_overlay_iv)
        attachmentUploadingDownloadingProgressBar =
            this.findViewById(R.id.attachment_downloading_pb)
        videoLength = this.findViewById(R.id.video_length_tv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
    }

    override fun onBind(msg: ChatMessage) {

        attachmentNameTV.text = msg.attachmentName
        videoLength.text = convertMicroSecondsToNormalFormat(msg.videoLength)
        textViewTime.text = msg.timestamp?.toDisplayText()

        loadThumbnail(msg)

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
                if (msg.attachmentCurrentlyBeingDownloaded) {
                    handleDownloadInProgress()
                } else {
                    handleVideoNotDownloaded()
                }
            }
        }
    }

    private fun loadThumbnail(msg: ChatMessage) {
        if (msg.thumbnailBitmap != null) {

            Glide.with(context)
                .load(msg.thumbnailBitmap)
                .placeholder(getCircularProgressDrawable())
                .into(imageView)
        } else if (msg.thumbnail != null) {

            val thumbnailStorageRef = storage.reference.child(msg.thumbnail!!)
            Glide.with(context)
                .load(thumbnailStorageRef)
                .placeholder(getCircularProgressDrawable())
                .into(imageView)
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

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
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
        val file = returnFileIfAlreadyDownloadedElseNull()

        if (file != null) {
            navigation.openFullScreenVideoDialogFragment(file.toUri())
        } else {
            downloadAttachment()
        }
    }

    private fun downloadAttachment() = GlobalScope.launch{

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

}

class InVideoMessageView(context: Context, attrs: AttributeSet?) :
    VideoMessageView("in", context, attrs)

class OutVideoMessageView(context: Context, attrs: AttributeSet?) :
    VideoMessageView("out", context, attrs)