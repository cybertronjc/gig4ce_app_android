package com.gigforce.modules.feature_chat.screens.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.models.ContactModel
import com.google.firebase.storage.FirebaseStorage
import android.text.Spannable

import android.text.style.ForegroundColorSpan

import android.text.SpannableStringBuilder
import android.text.Spanned

import android.graphics.Typeface

import android.text.style.TextAppearanceSpan

import android.content.res.ColorStateList

import android.text.SpannableString

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

    private var createNewGroup: Boolean = false

    private var searchText: String = ""

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_contact, parent, false)
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
//        if (originalContactsList.isEmpty()) {
        this.originalContactsList = contacts
        this.filteredContactsList = contacts
        notifyDataSetChanged()
//        } else {
//            val result = DiffUtil.calculateDiff(
//                    ContactsDiffUtilCallback(
//                            originalContactsList,
//                            contacts
//                    )
//            )
//            this.originalContactsList = contacts
//            this.filteredContactsList = contacts
//            result.dispatchUpdatesTo(this)
//        }
    }


    inner class ContactViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener{
        private var contactAvatarIV: GigforceImageView = itemView.findViewById(R.id.user_image_iv)
        private var contactNameTV: TextView = itemView.findViewById(R.id.user_name_tv)
        private var contactLastLiveTV: TextView = itemView.findViewById(R.id.last_online_time_tv)
        //private val contactSelectedTick: View = itemView.findViewById(R.id.user_selected_layout)
        private val contactSelectUnselectTick: ImageView = itemView.findViewById(R.id.select_unselect_icon)

        init {
            itemView.setOnClickListener(this)
//            itemView.setOnLongClickListener(this)
        }

        fun bindValues(contact: ContactModel) {
            val dataString = contact.name
            if (searchText != null && !searchText.isEmpty()) {
                val startPos: Int = dataString.toString().toLowerCase()?.indexOf(searchText.toLowerCase())
                val endPos: Int = startPos + searchText.length
                if (startPos != -1) {
                    val spannable: Spannable = SpannableString(dataString.toString())
                    val colorStateList =
                        ColorStateList(arrayOf(intArrayOf()), intArrayOf(context.getColor(R.color.colorPrimary)))
                    val textAppearanceSpan =
                        TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                    spannable.setSpan(
                        textAppearanceSpan,
                        startPos,
                        endPos,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    contactNameTV.text = spannable

                } else {
                    contactNameTV.text = contact.name
                }
            } else {
                contactNameTV.text = contact.name
            }
//            if (searchText.isNotEmpty() && filteredContactsList.contains(contact)){
//                    //color your text here
//                    var index: Int = dataString.toString().indexOf(searchText)
//                    while (index > 0) {
//                        sb = SpannableStringBuilder(dataString.toString())
//                        val fcs = ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)) //specify color here
//                        sb.setSpan(
//                            fcs,
//                            index,
//                            index + searchText.length,
//                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
//                        )
//                        index = dataString.toString().indexOf(searchText, index + 1)
//                    }
//                contactNameTV.text = sb
//            } else{
//                contactNameTV.text = contact.name
//            }


            val mobileWith91 = "+${contact.mobile}"
            contactLastLiveTV.text = mobileWith91.substring(0, 3) + "-" + mobileWith91.substring(3)

            if (!contact.imageThumbnailPathInStorage.isNullOrBlank()) {

                val profilePathRef = if (contact.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                    firebaseStorage.reference.child(contact.imageThumbnailPathInStorage!!)
                else
                    firebaseStorage.reference.child("profile_pics/${contact.imageThumbnailPathInStorage!!}")

                Glide.with(context)
                        .load(profilePathRef)
                        .placeholder(R.drawable.ic_user_2)
                        .circleCrop()
                        .into(contactAvatarIV)
            } else if (!contact.imagePathInStorage.isNullOrBlank()) {

                val profilePathRef = if (contact.imagePathInStorage!!.startsWith("profile_pics/"))
                    firebaseStorage.reference.child(contact.imagePathInStorage!!)
                else
                    firebaseStorage.reference.child("profile_pics/${contact.imagePathInStorage!!}")

                Glide.with(context)
                        .load(profilePathRef)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_2)
                        .into(contactAvatarIV)
            } else if (!contact.imageUrl.isNullOrBlank()) {
                contactAvatarIV.loadImageIfUrlElseTryFirebaseStorage(contact.imageUrl!!)
            } else {
                requestManager
                        .load(R.drawable.ic_user_2)
                        .placeholder(R.drawable.ic_user_2)
                        .into(contactAvatarIV)
            }

            if (createNewGroup){
                contactSelectUnselectTick.visible()
            }else{
                contactSelectUnselectTick.gone()
            }

            if (selectedContacts.contains(contact)) {
                //contactSelectedTick.visible()
                contactSelectUnselectTick.setImageDrawable(context.getDrawable(R.drawable.ic_icon_selected))
            } else {
                //contactSelectedTick.gone()
                contactSelectUnselectTick.setImageDrawable(context.getDrawable(R.drawable.ic_icon_unselected))
            }
        }

        override fun onClick(v: View?) {

            if (selectedContacts.isNotEmpty() || createNewGroup) {
                val pos = adapterPosition
                val contact = filteredContactsList[pos]
                if (selectedContacts.contains(contact)) {
                    //remove it
                    selectedContacts.remove(contact)
                    notifyItemChanged(pos)

                    onContactClickListener.onContactSelected(selectedContacts.size, filteredContactsList.size)
                } else {
                    selectedContacts.add(contact)
                    notifyItemChanged(pos)
                    onContactClickListener.onContactSelected(selectedContacts.size, filteredContactsList.size)
                }
            } else {
                onContactClickListener.onContactSelected(selectedContacts.size, filteredContactsList.size)
                onContactClickListener.contactClick(filteredContactsList[adapterPosition])
            }
        }

//        override fun onLongClick(v: View?): Boolean {
//            val pos = adapterPosition
//            val contact = filteredContactsList[pos]
//            if (selectedContacts.contains(contact)) {
//                //remove it
//                selectedContacts.remove(contact)
//                notifyDataSetChanged()
//
//                onContactClickListener.onContactSelected(selectedContacts.size, filteredContactsList.size)
//            } else {
//                selectedContacts.add(contact)
//                notifyDataSetChanged()
//                //notifyItemChanged(pos)
//                onContactClickListener.onContactSelected(selectedContacts.size, filteredContactsList.size)
//            }
//
//            if (selectedContacts.isNullOrEmpty()) {
//                stateCreateGroup(true)
//            }
//
//            return true
//        }
    }

    fun stateCreateGroup(createGroup: Boolean) {
        createNewGroup = createGroup
    }

    fun isStateCreateGroup(): Boolean {
        return createNewGroup
    }

    fun getSelectedItems(): MutableList<ContactModel> {
        return selectedContacts
    }

    fun clearSelectedContacts() {
        selectedContacts.clear()
        notifyDataSetChanged()
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
            searchText = charString
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