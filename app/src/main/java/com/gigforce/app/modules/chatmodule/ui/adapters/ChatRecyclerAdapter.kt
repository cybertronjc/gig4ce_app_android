package com.gigforce.app.modules.chatmodule.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.toDisplayText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.models.*
import java.time.LocalDate

class ChatRecyclerAdapter constructor(
    private val context: Context,
    private val requestManager: RequestManager,
    private val onMessageClickListener: OnChatMessageClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatMessages: List<ChatMessage> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_DATE -> DateItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recview_wallet_month, parent, false)
            )
            VIEW_TYPE_CHAT_TEXT -> TextMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text, parent, false)
            )
            VIEW_TYPE_CHAT_IMAGE -> ImageMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_image, parent, false)
            )
            VIEW_TYPE_CHAT_VIDEO -> VideoMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_text_with_video, parent, false)
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

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    fun updateChatMessages(newMessages: List<ChatMessage>) {
        //TODO implement diff util here
        this.chatMessages = newMessages
        notifyDataSetChanged()
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
                val textMessage = chatMessage as TextChatMessage

                val viewHolder = holder as TextMessageViewHolder
                viewHolder.bindValues(textMessage.toMessage())
            }
            VIEW_TYPE_CHAT_IMAGE -> {

                val chatMessage = chatMessages[position]
                val imageWithTextMessage = chatMessage as ImageChatMessage

                val viewHolder = holder as ImageMessageViewHolder
                viewHolder.bindValues(imageWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_VIDEO -> {

                val chatMessage = chatMessages[position]
                val videoWithTextMessage = chatMessage as VideoChatMessage

                val viewHolder = holder as VideoMessageViewHolder
                viewHolder.bindValues(videoWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_LOCATION -> {

                val chatMessage = chatMessages[position]
                val locationWithTextMessage = chatMessage as LocationChatMessage

                val viewHolder = holder as LocationMessageViewHolder
                viewHolder.bindValues(locationWithTextMessage.toMessage())
            }

            VIEW_TYPE_CHAT_CONTACT -> {

                val chatMessage = chatMessages[position]
                val contactWithTextMessage = chatMessage as ContactChatMessage

                val viewHolder = holder as ContactMessageViewHolder
                viewHolder.bindValues(contactWithTextMessage.toMessage())
            }
            VIEW_TYPE_CHAT_AUDIO -> {

                val chatMessage = chatMessages[position]
                val audioChatMessage = chatMessage as AudioChatMessage

                val viewHolder = holder as AudioMessageViewHolder
                viewHolder.bindValues(audioChatMessage.toMessage())
            }
            VIEW_TYPE_CHAT_DOCUMENT -> {
                val chatMessage = chatMessages[position]
                val documentChatMessage = chatMessage as DocumentChatMessage

                val viewHolder = holder as DocumentMessageViewHolder
                viewHolder.bindValues(documentChatMessage.toMessage())
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

    private inner class AudioMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(msg: Message) {
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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_VIDEO,
                currentPos,
                chatMessages[currentPos]
            )
        }
    }

    private inner class ContactMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(msg: Message) {
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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_VIDEO,
                currentPos,
                chatMessages[currentPos]
            )
        }
    }

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

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(msg: Message) {
            progressbar.isVisible = msg.attachmentPath == null

            when (msg.flowType) {
                "in" -> {
                    textView.text = msg.attachmentName
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
                    textView.text = msg.attachmentName
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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_DOCUMENT,
                currentPos,
                chatMessages[currentPos]
            )
        }
    }

    private inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)
        private val imageLoadingProgressBar: View = itemView.findViewById(R.id.loading_progress_bar)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(msg: Message) {

            if (msg.attachmentPath.isNullOrBlank()) {
                imageView.setImageDrawable(null)
                imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.white,
                        null
                    )
                )
                imageLoadingProgressBar.visible()
            } else {
                imageLoadingProgressBar.gone()

                if(msg.thumbnail.isNullOrBlank()) {
                    requestManager.load(msg.attachmentPath)
                        .placeholder(getCircularProgressDrawable()).into(imageView)
                } else{
                    requestManager.load(msg.thumbnail)
                        .placeholder(getCircularProgressDrawable()).into(imageView)
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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_IMAGE,
                currentPos,
                chatMessages[currentPos]
            )
        }
    }

    private inner class LocationMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)

        fun bindValues(msg: Message) {
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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_VIDEO,
                currentPos,
                chatMessages[currentPos]
            )
        }
    }

    class MessageTypeNotSupportedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val linearLayout: LinearLayout = itemView.findViewById(R.id.ll_msgContainer)
        private val textView: TextView = itemView.findViewById(R.id.tv_msgValue)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_msgTimeValue)
        private val cardView: CardView = itemView.findViewById(R.id.cv_msgContainer)

        fun bindValues(msg: Message) {
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
                chatMessages[currentPos]
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
        private val uploadingProgress: View = itemView.findViewById(R.id.uploading_progress)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(msg: Message) {

            if (msg.attachmentPath.isNullOrBlank()) {
                uploadingProgress.visible()
            } else {
                uploadingProgress.gone()
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

            attachmentNameTV.text = msg.attachmentName

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
            val currentPos = adapterPosition
            onMessageClickListener.chatMessageClicked(
                MessageType.TEXT_WITH_VIDEO,
                currentPos,
                chatMessages[currentPos]
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