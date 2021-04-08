package com.gigforce.modules.feature_chat.screens.adapters

import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ContactModel
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
//        if (originalContactsList.isEmpty()) {
        this.originalContactsList = contacts
        this.filteredContactsList = contacts
        notifyDataSetChanged()
//        } else {
//            val result = DiffUtil.calculateDiff(
//                ContactsDiffUtilCallback(
//                    originalContactsList,
//                    contacts
//                )
//            )
//            this.originalContactsList = contacts
//            this.filteredContactsList = contacts
//            result.dispatchUpdatesTo(this)
//        }
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

        private var contactAvatarIV: GigforceImageView = itemView.findViewById(R.id.user_image_iv)
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

            val mobileText = if (contact.mobile.startsWith("+91"))
                contact.mobile
            else if (contact.mobile.length == 10) {
                "+91${contact.mobile}"
            } else {
                "+${contact.mobile}"
            }
            val mobileWith91 = if(mobileText.length > 5) mobileText.substring(0, 3) + "-" + mobileText.substring(3) else ""

            if (contact.name.isNullOrBlank()) {
                contactNameTV.text = mobileWith91
                uidTV.text = ""
            } else {
                contactNameTV.text = contact.name
                uidTV.text = mobileWith91
            }



            isUserManagerView.isVisible = contact.isUserGroupManager
            val isUserTheCurrentUser = contact.uid == currentUserUid
            if (isUserTheCurrentUser) {
                chatOverlay.gone()
                chatIcon.gone()
            } else {
                chatOverlay.visible()
                chatIcon.visible()
            }



            if (!contact.imageThumbnailPathInStorage.isNullOrBlank()) {

                if (Patterns.WEB_URL.matcher(contact.imageThumbnailPathInStorage!!).matches()) {
                    contactAvatarIV.loadImageIfUrlElseTryFirebaseStorage(contact.imageThumbnailPathInStorage!!)
                } else {

                    val profilePathRef = if (contact.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                        contact.imageThumbnailPathInStorage!!
                    else
                        "profile_pics/${contact.imageThumbnailPathInStorage}"

                    contactAvatarIV.loadImageFromFirebase(profilePathRef)
                }
            } else if (!contact.imagePathInStorage.isNullOrBlank()) {

                if (Patterns.WEB_URL.matcher(contact.imagePathInStorage!!).matches()) {
                    contactAvatarIV.loadImageIfUrlElseTryFirebaseStorage(contact.imagePathInStorage!!)
                } else {

                    val profilePathRef = if (contact.imagePathInStorage!!.startsWith("profile_pics/"))
                        contact.imagePathInStorage!!
                    else
                        "profile_pics/${contact.imagePathInStorage}"
                    contactAvatarIV.loadImageFromFirebase(profilePathRef)
                }

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

    interface OnGroupMembersClickListener {

        fun onGroupMemberItemLongPressed(
                view: View,
                position: Int,
                contact: ContactModel
        )

        fun onChatIconClicked(
                position: Int,
                contact: ContactModel
        )
    }
}