package com.gigforce.app.modules.chatmodule.ui.adapters

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.ChatConstants
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.utils.TextDrawable
import java.text.SimpleDateFormat


class ChatListRecyclerAdapter(
        private val context: Context,
        private val requestManager: RequestManager,
        private val onChatItemClickListener: OnChatItemClickListener
) : RecyclerView.Adapter<ChatListRecyclerAdapter.ContactViewHolder>() {

    private var chatList: ArrayList<ChatHeader> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_header_item, parent, true)

        return ContactViewHolder(
                view,
                requestManager
        )
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindValues(chatList.get(position))
    }

    fun setData(chatHeaders: ArrayList<ChatHeader>) {
        chatList = chatHeaders
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(
            itemView: View,
            private val requestManager: RequestManager
    ) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var circleImageView: ImageView = itemView.findViewById(R.id.contact_image_view)
        private var textViewName: TextView = itemView.findViewById(R.id.tv_nameValue)
        private var txtSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private var textViewTime: TextView = itemView.findViewById(R.id.tv_timeValue)
        private var viewPinkCircle: View = itemView.findViewById(R.id.online_pink_circle)
        private var lastMessageType: ImageView = itemView.findViewById(R.id.last_mesage_type)
        private var unseenMessageCountIV: ImageView =
                itemView.findViewById(R.id.unseen_msg_count_iv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(chatHeader: ChatHeader) {

            if (chatHeader.unseenCount != 0) {
                val drawable = TextDrawable.builder().buildRound(
                        chatHeader.unseenCount.toString(),
                        ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                )
                unseenMessageCountIV.setImageDrawable(drawable)
            } else {
                unseenMessageCountIV.setImageDrawable(null)
            }

            if (chatHeader.chatType == ChatConstants.CHAT_TYPE_USER) {

                textViewName.text = chatHeader.otherUser!!.name
                val userAvatarUrl = chatHeader.otherUser?.profilePic
                if (userAvatarUrl.isNullOrBlank()) {
                    //Show Default User avatar
                    requestManager.load(R.drawable.ic_user_2).into(circleImageView)
                } else {
                    requestManager.load(userAvatarUrl).placeholder(R.drawable.ic_user_2).into(circleImageView)
                }

            } else if (chatHeader.chatType == ChatConstants.CHAT_TYPE_GROUP) {

                textViewName.text = chatHeader.groupName
                val userAvatarUrl = chatHeader.groupAvatar
                if (userAvatarUrl.isBlank()) {
                    //Show Default User avatar
                    requestManager.load(R.drawable.ic_group).into(circleImageView)
                } else {
                    requestManager.load(userAvatarUrl).into(circleImageView)
                }
            }

            if (chatHeader.otherUser!!.name == "Help") {
                textViewName.setTextColor(Color.parseColor("#E91E63"))
                viewPinkCircle.visibility = View.GONE
            } else {

                when (chatHeader.lastMessageType) {
                    Message.MESSAGE_TYPE_TEXT -> {
                        lastMessageType.gone()
                        txtSubtitle.text = chatHeader.lastMsgText
                    }
                    Message.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_play)
                        txtSubtitle.text = "Video"
                    }
                    Message.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_document_outlined)
                        txtSubtitle.text = "Document"
                    }
                    Message.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                        lastMessageType.visible()
                        lastMessageType.setImageResource(R.drawable.ic_document_outlined)
                        txtSubtitle.text = "Image"
                    }
                    else -> {
                        lastMessageType.gone()
                        txtSubtitle.text = ""
                    }
                }
            }

            val chatDate = chatHeader.lastMsgTimestamp?.toDate()
            if (chatDate != null) {

                if (DateUtils.isToday(chatDate.time)) {
                    textViewTime.text = SimpleDateFormat("hh:mm aa").format(chatDate)
                } else {
                    textViewTime.text = SimpleDateFormat("dd MMM").format(chatDate)
                }
            } else {
                textViewTime.text = null
            }
        }

        override fun onClick(v: View?) {
            onChatItemClickListener.onChatItemClicked(chatList[adapterPosition])
        }
    }


    interface OnChatItemClickListener {

        fun onChatItemClicked(chatHeader: ChatHeader)
    }

}