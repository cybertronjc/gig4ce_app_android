package com.gigforce.app.modules.chatmodule.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.modules.chatmodule.models.ContactModel

class ContactsRecyclerAdapter(
        private val requestManager: RequestManager,
        private val onContactClickListener: OnContactClickListener
) : RecyclerView.Adapter<ContactsRecyclerAdapter.ContactViewHolder>() {

    private var contactsList: List<ContactModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_chat_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindValues(contactsList.get(position))
    }

    fun setData(contacts: List<ContactModel>) {
        contactsList = if (HELP_CHAT_ENABLED) {
            contacts
                    .sortedBy { it.name }
                    .toMutableList()
                    .apply {
                        add(0, HELP_CHAT)
                    }
        } else {
            contacts.sortedBy { it.name }
        }
        notifyDataSetChanged()
    }


    inner class ContactViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var contactAvatarIV: ImageView = itemView.findViewById(R.id.user_image_iv)
        private var contactNameTV: TextView = itemView.findViewById(R.id.user_name_tv)
        private var contactLastLiveTV: TextView = itemView.findViewById(R.id.last_online_time_tv)

        init {
               itemView.setOnClickListener(this)
        }

        fun bindValues(contact: ContactModel) {
            contactNameTV.text = contact.name
        }

        override fun onClick(v: View?) {
            onContactClickListener.contactClick(contactsList[adapterPosition])
        }
    }

    companion object {

        private const val HELP_CHAT_ENABLED = false
        private val HELP_CHAT = ContactModel(
                id = "help_chat",
                name = "Help",
                mobile = ""
        )
    }
}