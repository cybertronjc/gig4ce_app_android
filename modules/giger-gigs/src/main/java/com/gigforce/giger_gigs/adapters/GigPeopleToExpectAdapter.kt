package com.gigforce.giger_gigs.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.giger_gigs.R
import com.gigforce.core.datamodels.gigpage.ContactPerson
import kotlinx.android.synthetic.main.recycler_item_gig_people_to_expect.view.*

interface GigPeopleToExpectAdapterClickListener {
    fun onPeopleToExpectClicked(option: ContactPerson)

    fun onCallManagerClicked(manager: ContactPerson)

    fun onChatWithManagerClicked(manager: ContactPerson)
}

class GigPeopleToExpectAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<GigPeopleToExpectAdapter.GigPeopleToExpectViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater
    private var otherOptionClickListener: GigPeopleToExpectAdapterClickListener? = null
    private var contactPerson: List<ContactPerson> = emptyList()

    fun setListener(otherOptionClickListener: GigPeopleToExpectAdapterClickListener) {
        this.otherOptionClickListener = otherOptionClickListener
    }

    fun updatePeopleToExpect(contactPerson: List<ContactPerson>) {
        this.contactPerson = contactPerson
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GigPeopleToExpectAdapter.GigPeopleToExpectViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return GigPeopleToExpectViewHolder(
            mLayoutInflater.inflate(
                R.layout.recycler_item_gig_people_to_expect,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GigPeopleToExpectViewHolder, position: Int) {

        val peopleToExpect = contactPerson[position]
        holder.bind(peopleToExpect)
    }

    override fun getItemCount() = contactPerson.size

    inner class GigPeopleToExpectViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val userImageTV = itemView.user_image_iv
        private val designationTV = itemView.designation_tv
        private val companyNameTV = itemView.company_name_tv
        private val userNameTV = itemView.user_name_tv
        private val callButton = itemView.call_btn
        private val chatButton = itemView.chat_btn

        init {
            itemView.setOnClickListener(this)
            callButton.setOnClickListener(this)
            chatButton.setOnClickListener(this)
        }

        fun bind(peopleToExpect: ContactPerson) = peopleToExpect.apply {

            if (this.profilePicture != null) {
                userImageTV.loadProfilePicture(
                    this.profilePicture,
                    this.profilePicture
                )
            } else {

                Glide.with(context.applicationContext)
                    .load(R.drawable.ic_avatar_male)
                    .circleCrop()
                    .into(userImageTV)
            }

            designationTV.text = this.designation
            userNameTV.text = this.name
            companyNameTV.text = this.companyName

            chatButton.isVisible = this.uid != null
        }

        override fun onClick(v: View?) {
            val viewClicked = v ?: return
            when (viewClicked.id) {
                R.id.call_btn -> otherOptionClickListener?.onCallManagerClicked(contactPerson[adapterPosition])
                R.id.chat_btn -> otherOptionClickListener?.onChatWithManagerClicked(contactPerson[adapterPosition])
                else -> {
                    if(adapterPosition>=0)
                    otherOptionClickListener?.onPeopleToExpectClicked(contactPerson[adapterPosition])
                }
            }
        }
    }

}