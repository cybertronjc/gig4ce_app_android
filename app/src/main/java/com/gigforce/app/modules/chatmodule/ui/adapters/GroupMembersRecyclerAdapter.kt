package com.gigforce.app.modules.chatmodule.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnGroupMembersClickListener
import com.gigforce.app.modules.chatmodule.ui.adapters.diffUtils.ContactsDiffUtilCallback
import com.google.firebase.auth.FirebaseAuth

class GroupMembersRecyclerAdapter(
    private val requestManager: RequestManager,
    private val onContactClickListener: OnGroupMembersClickListener
) : RecyclerView.Adapter<GroupMembersRecyclerAdapter.GroupMemberViewHolder>(),
    Filterable {

    private var isUserGroupManager: Boolean = false
    private var originalContactsList: List<ContactModel> = emptyList()
    private var filteredContactsList: List<ContactModel> = emptyList()
    private val contactsFilter = ContactsFilter()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_group_member_2, parent, false)
        return GroupMemberViewHolder(view)
    }

    fun setOrRemoveUserAsGroupManager(isUserGroupManager: Boolean) {
        this.isUserGroupManager = isUserGroupManager
    }

    override fun getItemCount(): Int {
        return filteredContactsList.size
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
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


    inner class GroupMemberViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener, View.OnClickListener {

        private var contactAvatarIV: ImageView = itemView.findViewById(R.id.user_image_iv)
        private var contactNameTV: TextView = itemView.findViewById(R.id.user_name_tv)
        private val uidTV: TextView = itemView.findViewById(R.id.last_online_time_tv)
        private val isUserManagerView: View = itemView.findViewById(R.id.manager_text_view)
        private val chatOverlay: View = itemView.findViewById(R.id.chat_overlay)
        private val chatIcon: View = itemView.findViewById(R.id.chat_icon)

        init {
            itemView.setOnLongClickListener(this)
            chatOverlay.setOnClickListener(this)
        }

        fun bindValues(contact: ContactModel) {
            contactNameTV.text = contact.name
            uidTV.text = contact.mobile
            isUserManagerView.isVisible = contact.isUserGroupManager
            val isUserTheCurrentUser = contact.uid == currentUserUid
            if(isUserTheCurrentUser){
                chatOverlay.gone()
                chatIcon.gone()
            } else{
                chatOverlay.visible()
                chatIcon.visible()
            }

            if (contact.imageUrl != null) {
                requestManager.load(contact.imageUrl!!).into(contactAvatarIV)
            } else {
                requestManager.load(R.drawable.ic_user_2).into(contactAvatarIV)
            }
        }

        override fun onLongClick(v: View?): Boolean {

            if (isUserGroupManager) {
                val contact = filteredContactsList[adapterPosition]
                if (contact.uid != currentUserUid) {
                    onContactClickListener.onGroupMemberItemLongPressed(
                        v!!,
                        adapterPosition,
                        contact
                    )
                }
            }
            return true
        }

        override fun onClick(v: View?) {
            val contact = filteredContactsList[adapterPosition]

            if (contact.uid != currentUserUid) {
                onContactClickListener.onChatIconClicked(
                    adapterPosition,
                    contact
                )
            }
        }

    }
}