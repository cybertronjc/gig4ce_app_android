package com.gigforce.modules.feature_chat.adapters

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.RequestManager
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.adapters.diffutils.ChatDiffUtilCallback
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.*
import java.io.File
import java.util.concurrent.TimeUnit

class OldChatRecyclerAdapter constructor(
    private val context: Context,
    private val refToGigForceAttachmentDirectory: File,
    private val requestManager: RequestManager,
    private val onMessageClickListener: OnChatMessageClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var oldChatMessages: MutableList<OldChatMessage> = mutableListOf()
    private var itemsDownloading: MutableList<OldChatMessage> = mutableListOf()

    private var imagesDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
    private var videosDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)
    private var documentsDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_DATE -> throw IllegalStateException("not in use currently")

            VIEW_TYPE_CHAT_TEXT -> TextMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text, parent, false)
            )
            VIEW_TYPE_CHAT_IMAGE -> ImageMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_image_in, parent, false)
            )
            VIEW_TYPE_CHAT_VIDEO -> VideoMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_video_in, parent, false)
            )
            VIEW_TYPE_CHAT_LOCATION -> LocationMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_location, parent, false)
            )
            VIEW_TYPE_CHAT_CONTACT -> ContactMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_contact, parent, false)
            )
            VIEW_TYPE_CHAT_AUDIO -> AudioMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_audio, parent, false)
            )
            VIEW_TYPE_CHAT_DOCUMENT -> DocumentMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_document, parent, false)
            )
            VIEW_TYPE_MESSAGE_TYPE_NOT_SUPPORTED -> MessageTypeNotSupportedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message_not_supported, parent, false)
            )
            else -> {
                throw IllegalArgumentException("ChatRecyclerAdapter:onCreateViewHolder() View type $viewType not supported")
            }
        }
    }

    fun setItemAsDownloading(index: Int) {
        if (oldChatMessages.size > index) {
            itemsDownloading.add(oldChatMessages[index])
            notifyDataSetChanged()
        }
    }

    fun setItemAsNotDownloading(index: Int) {
        if (oldChatMessages.size > index) {
            itemsDownloading.remove(oldChatMessages[index])
            notifyDataSetChanged()
        }
    }

    fun addItem(messageOld: OldChatMessage) {
        oldChatMessages.add(messageOld)
        notifyItemInserted(oldChatMessages.size - 1)
    }

    override fun getItemCount(): Int {
        return oldChatMessages.size
    }

    fun updateChatMessages(newMessageOlds: List<OldChatMessage>) {

        if (oldChatMessages.isEmpty()) {
            this.oldChatMessages = newMessageOlds.toMutableList()
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(
                ChatDiffUtilCallback(
                    oldChatMessages,
                    newMessageOlds
                )
            )
            this.oldChatMessages = newMessageOlds.toMutableList()
            result.dispatchUpdatesTo(this)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (oldChatMessages[position].getMessageType()) {
            MessageType.DATE -> VIEW_TYPE_DATE
            MessageType.TEXT -> VIEW_TYPE_CHAT_TEXT
            MessageType.TEXT_WITH_IMAGE -> VIEW_TYPE_CHAT_IMAGE
            MessageType.TEXT_WITH_VIDEO -> VIEW_TYPE_CHAT_VIDEO
            MessageType.TEXT_WITH_LOCATION -> VIEW_TYPE_CHAT_LOCATION
            MessageType.TEXT_WITH_CONTACT -> VIEW_TYPE_CHAT_CONTACT
            MessageType.TEXT_WITH_AUDIO -> VIEW_TYPE_CHAT_AUDIO
            MessageType.TEXT_WITH_DOCUMENT -> VIEW_TYPE_CHAT_DOCUMENT
            MessageType.NOT_SUPPORTED -> VIEW_TYPE_MESSAGE_TYPE_NOT_SUPPORTED
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            VIEW_TYPE_DATE -> {

                val chatMessage = oldChatMessages[position]
                val chatDateItem = chatMessage as ChatDateItem

                val viewHolder = holder as DateItemViewHolder
                //  viewHolder.bindDate(chatDateItem.getDate())
            }
            VIEW_TYPE_CHAT_TEXT -> {

                val chatMessage = oldChatMessages[position]
                val textMessage = chatMessage as TextChatMessage

                val viewHolder = holder as TextMessageViewHolder
                viewHolder.bindValues(textMessage.toMessage())
            }
            VIEW_TYPE_CHAT_IMAGE -> {

                val chatMessage = oldChatMessages[position]
                val imageWithTextMessage = chatMessage as ImageChatMessage

                val viewHolder = holder as ImageMessageViewHolder
                viewHolder.bindValues(
                    imageWithTextMessage,
                    imageWithTextMessage.toMessage()
                )
            }
            VIEW_TYPE_CHAT_VIDEO -> {

                val chatMessage = oldChatMessages[position]
                val videoWithTextMessage = chatMessage as VideoChatMessage

                val viewHolder = holder as VideoMessageViewHolder
                viewHolder.bindValues(videoWithTextMessage, videoWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_LOCATION -> {

                val chatMessage = oldChatMessages[position]
                val locationWithTextMessage = chatMessage as LocationChatMessage

                val viewHolder = holder as LocationMessageViewHolder
                //viewHolder.bindValues(locationWithTextMessage.toMessage())
            }

            VIEW_TYPE_CHAT_CONTACT -> {

                val chatMessage = oldChatMessages[position]
                val contactWithTextMessage = chatMessage as ContactChatMessage

                val viewHolder = holder as ContactMessageViewHolder
                // viewHolder.bindValues(contactWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_AUDIO -> {

                val chatMessage = oldChatMessages[position]
                val audioChatMessage = chatMessage as AudioChatMessage

                val viewHolder = holder as AudioMessageViewHolder
                // viewHolder.bindValues(audioChatMessage.toMessage())
            }
            VIEW_TYPE_CHAT_DOCUMENT -> {
                val chatMessage = oldChatMessages[position]
                val documentChatMessage = chatMessage as DocumentChatMessage

                val viewHolder = holder as DocumentMessageViewHolder
                viewHolder.bindValues(documentChatMessage, documentChatMessage.toMessage())
            }
            VIEW_TYPE_MESSAGE_TYPE_NOT_SUPPORTED -> {
                //DO nothing
            }
            else -> {
                throw IllegalArgumentException("ChatRecyclerAdapter:onBindViewHolder() invalid view type ${holder.itemViewType} passed")
            }
        }

    //------------
    //View Holders
    //----

    //Inactive holders
    private inner class AudioMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class ContactMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class DateItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class LocationMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    //Active Holders
    class MessageTypeNotSupportedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * ViewHolders
     * [TextMessageViewHolder]
     * [ImageMessageViewHolder]
     * [VideoMessageViewHolder]
     * [DocumentMessageViewHolder]
     */

    private inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)

        fun bindValues(msg: ChatMessage) {
            when (msg.flowType) {
                "in" -> {
                    textView.text = msg.content

                    textViewTime.text = msg.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#979797"))

                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textView.setTextColor(Color.parseColor("#000000"))


                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26, 5, 160, 5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textView.text = msg.content

                    textViewTime.text = msg.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))

                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textView.setTextColor(Color.parseColor("#ffffff"))


                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160, 5, 26, 5)
                    cardView.layoutParams = layoutParams
                }
            }
        }

        override fun onClick(v: View?) {
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_VIDEO,
                currentPos,
                oldChatMessages[currentPos],
                false,
                null
            )
        }
    }

    private inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private var downloadIconIV: ImageView = itemView.findViewById(R.id.download_icon_iv)
        private var downloadOverlayIV: ImageView = itemView.findViewById(R.id.download_overlay_iv)
        private val attachmentDownloadingProgressBar: ProgressBar =
            itemView.findViewById(R.id.attachment_downloading_pb)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(oldChatMessage: OldChatMessage, msg: ChatMessage) {

            if (msg.attachmentPath.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        android.R.color.white,
                        null
                    )
                )
                downloadIconIV.gone()
                downloadOverlayIV.visible()

                if (msg.thumbnailBitmap != null) {
                    requestManager
                        .load(msg.thumbnailBitmap)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

//                    msg.thumbnailBitmap?.let {
//
//                        if (!it.isRecycled)
//                            it.recycle()
//                    }
                }
                attachmentDownloadingProgressBar.visible()
            } else {
                attachmentDownloadingProgressBar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileIfAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_IMAGE,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(oldChatMessage)
                    attachmentDownloadingProgressBar.gone()
                    downloadOverlayIV.gone()

                    downloadIconIV.gone()
                    requestManager
                        .load(downloadedFile)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)
                } else {
                    val isFileDownloading = itemsDownloading.contains(oldChatMessage)
                    requestManager
                        .load(msg.thumbnail)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

                    if (isFileDownloading) {
                        downloadOverlayIV.visible()
                        downloadIconIV.gone()
                        attachmentDownloadingProgressBar.visible()
                    } else {
                        attachmentDownloadingProgressBar.gone()
                        downloadOverlayIV.visible()
                        downloadIconIV.visible()
                        requestManager
                            .load(R.drawable.ic_download_24)
                            .into(downloadIconIV)
                    }
                }
            }

            when (msg.flowType) {

                "in" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#979797"))
                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26, 5, 160, 5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160, 5, 26, 5)
                    cardView.layoutParams = layoutParams
                }
            }
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = oldChatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileIfAlreadyDownloadedElseNull(ChatConstants.ATTACHMENT_TYPE_IMAGE, fileName)

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_IMAGE,
                position = pos,
                messageOld = oldChatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }

    private inner class VideoMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val attachmentNameTV: TextView = itemView.findViewById(R.id.tv_file_name)
        private var playDownloadIconIV: ImageView =
            itemView.findViewById(R.id.play_download_icon_iv)
        private var playDownloadOverlayIV: ImageView =
            itemView.findViewById(R.id.play_download_overlay_iv)
        private val attachmentUploadingDownloadingProgressBar: ProgressBar =
            itemView.findViewById(R.id.attachment_downloading_pb)
        private val videoLength: TextView = itemView.findViewById(R.id.video_length_tv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(
                oldChatMessage: OldChatMessage,
                msg: ChatMessage
        ) {

            attachmentNameTV.text = msg.attachmentName
            videoLength.text = convertMicroSecondsToNormalFormat(msg.videoLength)

            if (msg.attachmentPath.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        android.R.color.white,
                        null
                    )
                )
                playDownloadOverlayIV.visible()
                playDownloadIconIV.gone()
                attachmentUploadingDownloadingProgressBar.visible()

                msg.thumbnailBitmap?.let {
                    requestManager
                        .load(it)
                        .into(imageView)
                }
            } else {
                attachmentUploadingDownloadingProgressBar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileIfAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_VIDEO,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(oldChatMessage)
                    attachmentUploadingDownloadingProgressBar.gone()

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    requestManager.load(R.drawable.ic_play_2).into(playDownloadIconIV)
                    requestManager.load(downloadedFile).placeholder(getCircularProgressDrawable())
                        .into(imageView)
                } else {
                    val isFileDownloading = itemsDownloading.contains(oldChatMessage)
                    requestManager.load(msg.thumbnail).placeholder(getCircularProgressDrawable())
                        .into(imageView)

                    if (isFileDownloading) {

                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.gone()
                        attachmentUploadingDownloadingProgressBar.visible()
                    } else {

                        attachmentUploadingDownloadingProgressBar.gone()
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.visible()
                        requestManager.load(R.drawable.ic_download_24).into(playDownloadIconIV)
                    }
                }
            }

            if (msg.thumbnail.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        android.R.color.white,
                        null
                    )
                )
            } else {
                requestManager.load(msg.thumbnail).into(imageView)
            }

            when (msg.flowType) {
                "in" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#979797"))
                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textViewTime.setTextColor(Color.parseColor("#000000"))
                    attachmentNameTV.setTextColor(Color.parseColor("#E91E63"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26, 5, 160, 5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))
                    attachmentNameTV.setTextColor(Color.parseColor("#ffffff"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160, 5, 26, 5)
                    cardView.layoutParams = layoutParams
                }
            }
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = oldChatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileIfAlreadyDownloadedElseNull(ChatConstants.ATTACHMENT_TYPE_VIDEO, fileName)

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_VIDEO,
                position = pos,
                messageOld = oldChatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }

    private inner class DocumentMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val linearLayout: ConstraintLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_file_name)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val progressbar: View = itemView.findViewById(R.id.progress)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(oldChatMessage: OldChatMessage, msg: ChatMessage) {


            if (msg.attachmentPath.isNullOrBlank()) {
                progressbar.visible()
            } else {
                progressbar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileIfAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_DOCUMENT,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(oldChatMessage)
                    progressbar.gone()
                } else {
                    val isFileDownloading = itemsDownloading.contains(oldChatMessage)

                    if (isFileDownloading) {
                        progressbar.visible()
                    } else {
                        progressbar.gone()
                    }
                }
            }

            textView.text = msg.attachmentName
            when (msg.flowType) {
                "in" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    textViewTime.setTextColor(Color.parseColor("#979797"))
                    linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                    textView.setTextColor(Color.parseColor("#000000"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(26, 5, 160, 5)
                    cardView.layoutParams = layoutParams
                }
                "out" -> {
                    textViewTime.text = msg.timestamp?.toDisplayText()
                    linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                    textView.setTextColor(Color.parseColor("#ffffff"))
                    textViewTime.setTextColor(Color.parseColor("#ffffff"))

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.gravity = Gravity.END
                    layoutParams.setMargins(160, 5, 26, 5)
                    cardView.layoutParams = layoutParams
                }
            }
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = oldChatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileIfAlreadyDownloadedElseNull(
                    ChatConstants.ATTACHMENT_TYPE_DOCUMENT,
                    fileName
                )

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_DOCUMENT,
                position = pos,
                messageOld = oldChatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }


    private fun returnFileIfAlreadyDownloadedElseNull(type: String, fileName: String): File? {
        if (type == ChatConstants.ATTACHMENT_TYPE_IMAGE) {
            val file = File(imagesDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        } else if (type == ChatConstants.ATTACHMENT_TYPE_VIDEO) {
            val file = File(videosDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        } else if (type == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
            val file = File(documentsDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        }

        throw IllegalArgumentException("other types not supported yet")
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

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }


    companion object {
        private const val VIEW_TYPE_DATE = 0
        private const val VIEW_TYPE_CHAT_TEXT = 1
        private const val VIEW_TYPE_CHAT_IMAGE = 2
        private const val VIEW_TYPE_CHAT_VIDEO = 3
        private const val VIEW_TYPE_CHAT_LOCATION = 4
        private const val VIEW_TYPE_CHAT_CONTACT = 5
        private const val VIEW_TYPE_CHAT_AUDIO = 6
        private const val VIEW_TYPE_CHAT_DOCUMENT = 7
        private const val VIEW_TYPE_MESSAGE_TYPE_NOT_SUPPORTED = 8
    }
}