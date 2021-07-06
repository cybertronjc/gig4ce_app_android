package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.extensions.dp
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
abstract class ImageMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(
        context,
        attrs
), View.OnClickListener,
    View.OnLongClickListener,
    PopupMenu.OnMenuItemClickListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private lateinit var senderNameTV: TextView
    private lateinit var imageView: ImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var downloadIconIV: ImageView
    private lateinit var downloadOverlayIV: ImageView
    private lateinit var attachmentDownloadingProgressBar: ProgressBar
    private lateinit var receivedStatusIV: ImageView
    private lateinit var imageContainerFrameLayout: FrameLayout

    //Data
    private lateinit var chatMessage: ChatMessage

    init {
        setDefault()
        inflate()
        findViews()
        setOnClickListeners()
    }

    private fun findViews() {

        senderNameTV = this.findViewById(R.id.user_name_tv)
        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        downloadIconIV = this.findViewById(R.id.download_icon_iv)
        downloadOverlayIV = this.findViewById(R.id.download_overlay_iv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        attachmentDownloadingProgressBar = this.findViewById(R.id.attachment_downloading_pb)
        imageContainerFrameLayout = this.findViewById(R.id.image_container_layout)
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
        cardView.setOnLongClickListener(this)
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
                    .centerCrop()
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
            loadThumbnail(msg)
        } else {

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            if (downloadedFile != null) {
                handleImageDownloaded(downloadedFile)
            } else {
                loadThumbnail(msg)
                handleImageNotDownloaded()
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
        chatMessage = msg
        textViewTime.text = msg.timestamp?.toDisplayText()

        senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
        senderNameTV.text = msg.senderInfo.name

        adjustImageSizeAcc(msg)
        handleImage(msg)
        setReceivedStatus(msg)
    }

    private fun adjustImageSizeAcc(msg: ChatMessage) {
        if (msg.imageMetaData == null) {
            imageContainerFrameLayout.layoutParams.width = 196.dp
            imageContainerFrameLayout.layoutParams.height = 196.dp
        } else {
            val imageMetaData = msg.imageMetaData!!
            if (imageMetaData.size.height == 0 ||
                    imageMetaData.size.height == imageMetaData.size.width) {
                imageContainerFrameLayout.layoutParams.width = 196.dp
                imageContainerFrameLayout.layoutParams.height = 196.dp
            } else {
                val isHeightBiggerThanWidth = imageMetaData.size.height > imageMetaData.size.width
                if (isHeightBiggerThanWidth) {
                    val aspectRatio = (imageMetaData.size.height / imageMetaData.size.width.toFloat())
                    val newHeight = (196 * aspectRatio).toInt().dp
                    val layoutParams =  imageContainerFrameLayout.layoutParams
                    layoutParams.width = 196.dp
                    layoutParams.height = newHeight
                    imageContainerFrameLayout.layoutParams = layoutParams
                } else {
                    val aspectRatio = (imageMetaData.size.width / imageMetaData.size.height.toFloat())
                    val newWidth = (196 * aspectRatio).toInt().dp
                    val layoutParams =  imageContainerFrameLayout.layoutParams
                    layoutParams.width = newWidth
                    layoutParams.height = 196.dp
                    imageContainerFrameLayout.layoutParams = layoutParams
                }
            }
        }
    }

    fun getCircularProgressDrawable(): Drawable {
        return ShimmerHelper.getShimmerDrawable()
    }

    override fun onClick(v: View?) {
        val file = returnFileIfAlreadyDownloadedElseNull()

        if (file != null) {
            chatNavigation.openFullScreenImageViewDialogFragment(file.toUri())
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

    override fun onLongClick(v: View?): Boolean {
        val popUpMenu = PopupMenu(context, v)
        popUpMenu.inflate(R.menu.menu_chat_clipboard)

        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
        popUpMenu.menu.findItem(R.id.action_delete).isVisible = type == MessageFlowType.OUT
        popUpMenu.menu.findItem(R.id.action_message_info).isVisible = type == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE

        popUpMenu.setOnMenuItemClickListener(this)
        popUpMenu.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> {
            }
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
        }
        return true
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