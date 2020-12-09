package com.gigforce.app.modules.chatmodule.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnContactClickListener
import com.gigforce.app.modules.chatmodule.ui.adapters.diffUtils.ContactsDiffUtilCallback

class ContactsRecyclerAdapter(
    private val context: Context,
    private val requestManager: RequestManager,
    private val onContactClickListener: OnContactClickListener
) : RecyclerView.Adapter<ContactsRecyclerAdapter.ContactViewHolder>(),
    Filterable {

    private var originalContactsList: List<ContactModel> = emptyList()
    private var filteredContactsList: List<ContactModel> = emptyList()

    private var selectedContacts: MutableList<ContactModel> = mutableListOf()
    private val contactsFilter = ContactsFilter()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_chat_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredContactsList.size
    }

    fun getSelectedContact(): List<ContactModel> = selectedContacts

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindValues(filteredContactsList.get(position))
    }



    fun setData(contacts: List<ContactModel>) {
        if (originalContactsList.isEmpty()) {
            this.originalContactsList = contacts
            this.filteredContactsList = contacts
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(
                ContactsDiffUtilCallback(
                    originalContactsList,
                    contacts
                )
            )
            this.originalContactsList = contacts
            this.filteredContactsList = contacts
            result.dispatchUpdatesTo(this)
        }
    }


    inner class ContactViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        View.OnLongClickListener {
        private var contactAvatarIV: ImageView = itemView.findViewById(R.id.user_image_iv)
        private var contactNameTV: TextView = itemView.findViewById(R.id.user_name_tv)
        private var contactLastLiveTV: TextView = itemView.findViewById(R.id.last_online_time_tv)
        private val contactSelectedTick: View = itemView.findViewById(R.id.user_selected_layout)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bindValues(contact: ContactModel) {
            contactNameTV.text = contact.name
            contactLastLiveTV.text = contact.mobile

            if (contact.imageUrl != null) {
                requestManager
                    .load(contact.imageUrl!!)
                    .placeholder(getCircularProgressDrawable())
                    .into(contactAvatarIV)
            } else {
                requestManager
                    .load(R.drawable.ic_user)
                    .into(contactAvatarIV)
            }

            if (selectedContacts.contains(contact)) {
                contactSelectedTick.visible()
            } else {
                contactSelectedTick.gone()
            }
        }

        override fun onClick(v: View?) {

            if (selectedContacts.isNotEmpty()) {
                val pos = adapterPosition
                val contact = filteredContactsList[pos]
                if (selectedContacts.contains(contact)) {
                    //remove it
                    selectedContacts.remove(contact)
                    notifyItemChanged(pos)

                    onContactClickListener.onContactSelected(selectedContacts.size)
                } else {
                    selectedContacts.add(contact)
                    notifyItemChanged(pos)
                    onContactClickListener.onContactSelected(selectedContacts.size)
                }
            } else {
                onContactClickListener.onContactSelected(selectedContacts.size)
                onContactClickListener.contactClick(filteredContactsList[adapterPosition])
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val pos = adapterPosition
            val contact = filteredContactsList[pos]
            if (selectedContacts.contains(contact)) {
                //remove it
                selectedContacts.remove(contact)
                notifyItemChanged(pos)

                onContactClickListener.onContactSelected(selectedContacts.size)
            } else {
                selectedContacts.add(contact)
                notifyItemChanged(pos)
                onContactClickListener.onContactSelected(selectedContacts.size)
            }
            return true
        }
    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class ContactsFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                filteredContactsList = originalContactsList
            } else {
                val filteredList: MutableList<ContactModel> = mutableListOf()
                for (contact in originalContactsList) {
                    if (contact.name?.contains(
                            charString,
                            true
                        ) == true || contact.mobile.contains(charString, true)
                    )
                        filteredList.add(contact)
                }
                filteredContactsList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = filteredContactsList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredContactsList = results?.values as List<ContactModel>
            notifyDataSetChanged()
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