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
import java.io.File
import javax.inject.Inject


abstract class ImageMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(
        context,
        attrs
), View.OnClickListener {

    private lateinit var imageView: ImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var downloadIconIV: ImageView
    private lateinit var downloadOverlayIV: ImageView
    private lateinit var attachmentDownloadingProgressBar: ProgressBar
    private lateinit var receivedStatusIV: ImageView

    @Inject
    lateinit var navigation: IChatNavigation


    init {
        setDefault()
        inflate()
        findViews()
        setOnClickListeners()

        (this.context.applicationContext as ChatModuleProvider)
                .provideChatModule()
                .inject(this)
        navigation.context = context
    }

    private fun findViews() {

        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        downloadIconIV = this.findViewById(R.id.download_icon_iv)
        downloadOverlayIV = this.findViewById(R.id.download_overlay_iv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        attachmentDownloadingProgressBar = this.findViewById(R.id.attachment_downloading_pb)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val resId =
                if (type == MessageFlowType.IN) R.layout.recycler_item_chat_text_with_image_in else R.layout.recycler_item_chat_text_with_image_out
        LayoutInflater.from(context).inflate(resId, this, true)
    }

    private fun setOnClickListeners() {
        cardView.setOnClickListener(this)
    }

    private fun handleImageNotDownloaded() {
        attachmentDownloadingProgressBar.gone()
        downloadOverlayIV.visible()
        downloadIconIV.visible()
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

    private fun handleDownloadInProgress() {
        downloadOverlayIV.visible()
        downloadIconIV.gone()
        attachmentDownloadingProgressBar.visible()
    }


    private fun handleImage(msg: ChatMessage) {
        imageView.setImageDrawable(null)

        if (msg.thumbnailBitmap != null) {
            handleImageUploading()
        } else {

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            if (downloadedFile != null) {
                handleImageDownloaded(downloadedFile)
            } else {
                loadThumbnail(msg)

//                if (msg.attachmentCurrentlyBeingDownloaded) {
//                    handleDownloadInProgress()
//                } else {
//                    handleImageNotDownloaded()
//                }
            }
        }
    }

    private fun handleImageUploading() {
        attachmentDownloadingProgressBar.visible()
        downloadOverlayIV.visible()
        downloadIconIV.gone()
    }

    private fun handleImageDownloaded(downloadedFile: File) {

        attachmentDownloadingProgressBar.gone()
        downloadOverlayIV.gone()
        downloadIconIV.gone()

        Glide.with(context)
                .load(downloadedFile)
                .placeholder(getCircularProgressDrawable())
                .into(imageView)
    }

    override fun onBind(msg: ChatMessage) {
        textViewTime.text = msg.timestamp?.toDisplayText()
        handleImage(msg)
        setReceivedStatus(msg)
    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    override fun onClick(v: View?) {
        val file = returnFileIfAlreadyDownloadedElseNull()

        if (file != null) {
            navigation.openFullScreenImageViewDialogFragment(file.toUri())
        } else {
            downloadAttachment()
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


    private fun downloadAttachment() = GlobalScope.launch {

        this.launch(Dispatchers.Main) {
            handleDownloadInProgress()
        }

        try {
            val file = downloadMediaFile()

            this.launch(Dispatchers.Main) {
                handleImageDownloaded(file)
            }
        } catch (e: Exception) {
        }
    }
}

class InImageMessageView(
        context: Context,
        attrs: AttributeSet?
) : ImageMessageView(
        MessageFlowType.IN,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)

class OutImageMessageView(
        context: Context,
        attrs: AttributeSet?
) : ImageMessageView(
        MessageFlowType.OUT,
        MessageType.ONE_TO_ONE_MESSAGE,
        context,
        attrs
)


class GroupInImageMessageView(
        context: Context,
        attrs: AttributeSet?
) : ImageMessageView(
        MessageFlowType.IN,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)

class GroupOutImageMessageView(
        context: Context,
        attrs: AttributeSet?
) : ImageMessageView(
        MessageFlowType.OUT,
        MessageType.GROUP_MESSAGE,
        context,
        attrs
)