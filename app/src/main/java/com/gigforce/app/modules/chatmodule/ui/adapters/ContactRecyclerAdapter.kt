package com.gigforce.app.modules.chatmodule.ui.adapters

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.modules.chatmodule.models.ChatHeader

class ContactRecyclerAdapter(private val requestManager: RequestManager,private val onContactClickListener: OnContactClickListener) : RecyclerView.Adapter<ContactRecyclerAdapter.ContactViewHolder>()
{
    private var contactsList : ArrayList<ChatHeader>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_contact_item,parent,false)
        return ContactViewHolder(
            view,
            requestManager,
            onContactClickListener
        )
    }

    override fun getItemCount(): Int {
        return contactsList?.size?:0
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindValues(contactsList?.get(position))
    }

    fun setData(chatHeaders : ArrayList<ChatHeader>){
        contactsList = chatHeaders
        notifyDataSetChanged()
    }


    class ContactViewHolder(itemView : View,
                            private val requestManager: RequestManager,
                            private val onContactClickListener: OnContactClickListener) : RecyclerView.ViewHolder(itemView)
    {
        private var circleImageView : ImageView = itemView.findViewById(R.id.contactImage)
        private var textViewName : TextView = itemView.findViewById(R.id.tv_nameValue)
        private var textViewDate : TextView = itemView.findViewById(R.id.tv_dateValue)
        private var textViewTime : TextView = itemView.findViewById(R.id.tv_timeValue)
        private var viewPinkCircle : View = itemView.findViewById(R.id.view_pinkCircle)
        fun bindValues(chatHeader : ChatHeader?){
            val uri = Uri.parse(
                "android.resource://com.gigforce.app/drawable/" +
                        chatHeader?.otherUser!!.profilePic)
            requestManager.load(uri).into(circleImageView)
            if (chatHeader?.otherUser!!.name == "Help"){
                textViewName.setTextColor(Color.parseColor("#E91E63"))
                viewPinkCircle.visibility = View.GONE
            }
            textViewName.text = chatHeader?.otherUser?.name?:""
            textViewDate.text = chatHeader?.lastMsgTimestamp?.toDate()?.date.toString() ?:""
            textViewTime.text = chatHeader?.lastMsgTimestamp?.toDate()?.time.toString() ?:""

            itemView.setOnClickListener {
                chatHeader.let {
                    onContactClickListener.contactClick(it.otherUser!!.profilePic,it.otherUser!!.name, it.id)
                }
            }
        }
    }
}