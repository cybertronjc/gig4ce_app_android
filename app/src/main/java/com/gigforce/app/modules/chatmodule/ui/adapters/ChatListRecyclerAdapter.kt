package com.gigforce.app.modules.chatmodule.ui.adapters

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.modules.chatmodule.models.ChatHeader
import java.text.SimpleDateFormat


class ChatListRecyclerAdapter(
    private val requestManager: RequestManager,
    private val onChatItemClickListener: OnChatItemClickListener
) : RecyclerView.Adapter<ChatListRecyclerAdapter.ContactViewHolder>() {

    private var chatList: ArrayList<ChatHeader> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_header_item, parent, false)

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

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(chatHeader: ChatHeader) {

            val userAvatarUrl = chatHeader.otherUser?.profilePic
            if (userAvatarUrl.isNullOrBlank()) {
                //Show Default User avatar
            } else {
                requestManager.load(userAvatarUrl).into(circleImageView)
            }

            if (chatHeader.otherUser!!.name == "Help") {
                textViewName.setTextColor(Color.parseColor("#E91E63"))
                viewPinkCircle.visibility = View.GONE
            }

            textViewName.text = chatHeader.otherUser.name
            txtSubtitle.text = chatHeader.lastMsgText

            val chatDate = chatHeader.lastMsgTimestamp?.toDate()
            if (chatDate != null) {

                if (DateUtils.isToday(chatDate.time)) {
                    textViewTime.text = SimpleDateFormat("hh:mm").format(chatDate)
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

    interface OnChatItemClickListener{

        fun onChatItemClicked(chatHeader: ChatHeader)
    }

}