package com.gigforce.app.modules.chatmodule.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.toDisplayText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.ChatConstants
import com.gigforce.app.modules.chatmodule.models.*
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnGroupChatMessageClickListener
import com.gigforce.app.modules.chatmodule.ui.adapters.diffUtils.GroupChatDiffUtilCallback
import com.gigforce.core.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class GroupChatRecyclerAdapter constructor(
    private val context: Context,
    private val refToGigForceAttachmentDirectory: File,
    private val requestManager: RequestManager,
    private val onMessageClickListener: OnGroupChatMessageClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatMessages: MutableList<GroupChatMessage> = mutableListOf()
    private var itemsDownloading: MutableList<GroupChatMessage> = mutableListOf()
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private val imagesDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
    private val videosDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)
    private val documentsDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_DATE -> DateItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recview_wallet_month, parent, false)
            )
            VIEW_TYPE_CHAT_TEXT -> TextMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_chat_text, parent, false)
            )
            VIEW_TYPE_CHAT_IMAGE -> ImageMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_chat_text_with_image, parent, false)
            )
            VIEW_TYPE_CHAT_VIDEO -> VideoMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_chat_text_with_video, parent, false)
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
                    .inflate(R.layout.item_group_chat_text_with_document, parent, false)
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

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    fun addItem(message: GroupChatMessage) {
        chatMessages.add(message)
        notifyItemInserted(chatMessages.size - 1)
    }

    fun updateChatMessages(newMessages: List<GroupChatMessage>) {

        if (chatMessages.isEmpty()) {
            this.chatMessages = newMessages.toMutableList()
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(
                GroupChatDiffUtilCallback(
                    chatMessages,
                    newMessages
                )
            )
            this.chatMessages = newMessages.toMutableList()
            result.dispatchUpdatesTo(this)
        }
    }

    fun setItemAsDownloading(index: Int) {
        if (chatMessages.size > index) {
            itemsDownloading.add(chatMessages[index])
            notifyDataSetChanged()
        }
    }

    fun setItemAsNotDownloading(index: Int) {
        if (chatMessages.size > index) {
            itemsDownloading.remove(chatMessages[index])
            notifyDataSetChanged()
        }
    }


    override fun getItemViewType(position: Int): Int =
        when (chatMessages[position].getMessageType()) {
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

                val chatMessage = chatMessages[position]
                val chatDateItem = chatMessage as ChatDateItem

                val viewHolder = holder as DateItemViewHolder
                viewHolder.bindDate(chatDateItem.getDate())
            }
            VIEW_TYPE_CHAT_TEXT -> {

                val chatMessage = chatMessages[position]
                val textMessage = chatMessage as GroupTextChatMessage

                val viewHolder = holder as TextMessageViewHolder
                viewHolder.bindValues(textMessage.toMessage())
            }
            VIEW_TYPE_CHAT_IMAGE -> {

                val chatMessage = chatMessages[position]
                val imageWithTextMessage = chatMessage as GroupImageChatMessage

                val viewHolder = holder as ImageMessageViewHolder
                viewHolder.bindValues(imageWithTextMessage,imageWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_VIDEO -> {

                val chatMessage = chatMessages[position]
                val videoWithTextMessage = chatMessage as GroupVideoChatMessage

                val viewHolder = holder as VideoMessageViewHolder
                viewHolder.bindValues(videoWithTextMessage,videoWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_LOCATION -> {

                val chatMessage = chatMessages[position]
                val locationWithTextMessage = chatMessage as LocationChatMessage

                val viewHolder = holder as LocationMessageViewHolder
                //viewHolder.bindValues(locationWithTextMessage.toMessage())
            }

            VIEW_TYPE_CHAT_CONTACT -> {

                val chatMessage = chatMessages[position]
                val contactWithTextMessage = chatMessage as ContactChatMessage

                val viewHolder = holder as ContactMessageViewHolder
              //  viewHolder.bindValues(contactWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_AUDIO -> {

                val chatMessage = chatMessages[position]
                val audioChatMessage = chatMessage as AudioChatMessage

                val viewHolder = holder as AudioMessageViewHolder
               // viewHolder.bindValues(audioChatMessage.toMessage())
            }
            VIEW_TYPE_CHAT_DOCUMENT -> {
                val chatMessage = chatMessages[position]
                val documentChatMessage = chatMessage as GroupDocumentChatMessage

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

    private inner class AudioMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class ContactMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class LocationMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class MessageTypeNotSupportedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private inner class DateItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindDate(date: LocalDate) {
            //
        }
    }



    private inner class DocumentMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val linearLayout: ConstraintLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_file_name)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val progressbar: View = itemView.findViewById(R.id.progress)
        private val senderTv: TextView = itemView.findViewById(R.id.user_name_tv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(groupChatMessage : GroupChatMessage, msg: GroupMessage) {
            progressbar.isVisible = msg.attachmentPath == null

            if (msg.attachmentPath.isNullOrBlank()) {
                progressbar.visible()
            } else {
                progressbar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_DOCUMENT,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(groupChatMessage)
                    progressbar.gone()
                } else {
                    val isFileDownloading = itemsDownloading.contains(groupChatMessage)

                    if (isFileDownloading) {
                        progressbar.visible()
                    } else {
                        progressbar.gone()
                    }
                }
            }

            textView.text = msg.attachmentName

            if (msg.senderInfo?.id == currentUserId) {
                senderTv.gone()
                senderTv.text = null

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

            } else {
                senderTv.visible()
                senderTv.text = msg.senderInfo?.name

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
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = chatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(ChatConstants.ATTACHMENT_TYPE_DOCUMENT, fileName)

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_DOCUMENT,
                position = pos,
                message = chatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }

    private inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val attachmentUploadingDownloadingProgressBar: View = itemView.findViewById(R.id.loading_progress_bar)
        private val senderTv: TextView = itemView.findViewById(R.id.user_name_tv)
        private var downloadIconIV: ImageView = itemView.findViewById(R.id.download_icon_iv)
        private var downloadOverlayIV: ImageView = itemView.findViewById(R.id.download_overlay_iv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(groupChatMessage: GroupChatMessage,msg: GroupMessage) {

            if (msg.attachmentPath.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.white,
                        null
                    )
                )
                downloadIconIV.gone()
                downloadOverlayIV.visible()
                attachmentUploadingDownloadingProgressBar.visible()
            } else {
                attachmentUploadingDownloadingProgressBar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_IMAGE,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(groupChatMessage)
                    attachmentUploadingDownloadingProgressBar.gone()
                    downloadOverlayIV.gone()
                    downloadIconIV.gone()

                    requestManager
                        .load(downloadedFile)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)
                } else {
                    val isFileDownloading = itemsDownloading.contains(groupChatMessage)
                    requestManager
                        .load(msg.thumbnail)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

                    if (isFileDownloading) {
                        downloadOverlayIV.visible()
                        downloadIconIV.gone()
                        attachmentUploadingDownloadingProgressBar.visible()
                    } else {
                        attachmentUploadingDownloadingProgressBar.gone()
                        downloadOverlayIV.visible()
                        downloadIconIV.visible()
                        requestManager
                            .load(R.drawable.ic_download_24)
                            .into(downloadIconIV)
                    }
                }
            }

            if (msg.senderInfo?.id == currentUserId) {
                textViewTime.text = msg.timestamp?.toDisplayText()
                linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                textViewTime.setTextColor(Color.parseColor("#ffffff"))
                senderTv.gone()
                senderTv.text = null

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.END
                layoutParams.setMargins(160, 5, 26, 5)
                cardView.layoutParams = layoutParams
            } else {
                textViewTime.text = msg.timestamp?.toDisplayText()
                senderTv.visible()
                senderTv.text = msg.senderInfo?.name

                textViewTime.setTextColor(Color.parseColor("#979797"))
                linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(26, 5, 160, 5)
                cardView.layoutParams = layoutParams
            }
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = chatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(ChatConstants.ATTACHMENT_TYPE_IMAGE, fileName)

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_IMAGE,
                position = pos,
                message = chatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }


    private inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val senderTv: TextView = itemView.findViewById(R.id.user_name_tv)

        fun bindValues(msg: GroupMessage) {

            if (msg.senderInfo?.id == currentUserId) {
                textView.text = msg.content
                senderTv.gone()
                senderTv.text = null

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
            } else {

                textView.text = msg.content
                senderTv.visible()
                senderTv.text = msg.senderInfo?.name

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
        }

        override fun onClick(v: View?) {
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT,
                currentPos,
                chatMessages[currentPos],
                false,
                null
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
        private val attachmentUploadingDownloadingProgressBar: View = itemView.findViewById(R.id.uploading_progress)
        private var playDownloadIconIV: ImageView = itemView.findViewById(R.id.play_button)
        private var playDownloadOverlayIV: ImageView = itemView.findViewById(R.id.download_overlay_iv)
        private val senderTv: TextView = itemView.findViewById(R.id.user_name_tv)
        private val videoLength: TextView = itemView.findViewById(R.id.video_length_tv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(groupChatMessage : GroupChatMessage,msg: GroupMessage) {

            attachmentNameTV.text = msg.attachmentName
            videoLength.text = convertMicroSecondsToNormalFormat(msg.videoAttachmentLength)

            if (msg.attachmentPath.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.white,
                        null
                    )
                )
                playDownloadOverlayIV.visible()
                playDownloadIconIV.gone()
                attachmentUploadingDownloadingProgressBar.visible()
            } else {
                attachmentUploadingDownloadingProgressBar.gone()

                val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
                val downloadedFile =
                    returnFileAlreadyDownloadedElseNull(
                        ChatConstants.ATTACHMENT_TYPE_VIDEO,
                        fileName
                    )
                val fileHasBeenDownloaded = downloadedFile != null

                if (fileHasBeenDownloaded) {
                    itemsDownloading.remove(groupChatMessage)
                    attachmentUploadingDownloadingProgressBar.gone()

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    requestManager.load(R.drawable.ic_play_2).into(playDownloadIconIV)
                    requestManager.load(downloadedFile).placeholder(getCircularProgressDrawable()).into(imageView)
                } else {
                    val isFileDownloading = itemsDownloading.contains(groupChatMessage)
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
                        R.color.white,
                        null
                    )
                )
            } else {
                requestManager.load(msg.thumbnail).into(imageView)
            }

            if(msg.senderInfo?.id != currentUserId){
                senderTv.visible()
                senderTv.text = msg.senderInfo?.name

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
            } else{
                senderTv.gone()
                senderTv.text = null

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

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val message = chatMessages[pos].toMessage()

            val attachmentPath = message.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(ChatConstants.ATTACHMENT_TYPE_VIDEO, fileName)

            onMessageClickListener.chatMessageClicked(
                messageType = MessageType.TEXT_WITH_VIDEO,
                position = pos,
                message = chatMessages[pos],
                fileDownloaded = downloadedFile != null,
                downloadedFile = downloadedFile
            )
        }
    }

    private fun returnFileAlreadyDownloadedElseNull(type: String, fileName: String): File? {
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

        throw IllegalArgumentException("other types not supperted yet")
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