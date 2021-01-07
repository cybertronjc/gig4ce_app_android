package com.gigforce.app.modules.gigPage2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage.models.GigPeopleToExpect
import kotlinx.android.synthetic.main.recycler_item_gig_people_to_expect.view.*

interface GigPeopleToExpectAdapterClickListener {
    fun onPeopleToExpectClicked(option: GigPeopleToExpect)
}

class GigPeopleToExpectAdapter(
        private val context: Context
) :
        RecyclerView.Adapter<GigPeopleToExpectAdapter.GigPeopleToExpectViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater
    private var otherOptionClickListener: GigPeopleToExpectAdapterClickListener? = null
    private var gigPeopleToExpect: List<GigPeopleToExpect> = emptyList()

    fun setListener(otherOptionClickListener: GigPeopleToExpectAdapterClickListener) {
        this.otherOptionClickListener = otherOptionClickListener
    }

    fun updatePeopleToExpect(gigPeopleToExpect: List<GigPeopleToExpect>) {
        this.gigPeopleToExpect = gigPeopleToExpect
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GigPeopleToExpectAdapter.GigPeopleToExpectViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return GigPeopleToExpectViewHolder(
                mLayoutInflater.inflate(
                        R.layout.recycler_item_gig_people_to_expect,
                        parent,
                        false
                ), viewType
        )
    }

    override fun onBindViewHolder(holder: GigPeopleToExpectViewHolder, position: Int) {

        val peopleToExpect = gigPeopleToExpect[position]
        holder.bind(peopleToExpect)
    }

    override fun getItemCount() = gigPeopleToExpect.size

    inner class GigPeopleToExpectViewHolder(itemView: View, viewType: Int) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        private val userImageTV = itemView.user_image_iv
        private val designationTV = itemView.designation_tv
        private val userNameTV = itemView.user_name_tv

        fun bind(peopleToExpect: GigPeopleToExpect) = peopleToExpect.apply {

            if (this.profilePicture != null) {

                Glide.with(context.applicationContext)
                        .load(this.profilePicture)
                        .circleCrop()
                        .into(userImageTV)
            } else {

                Glide.with(context.applicationContext)
                        .load(R.drawable.avatar)
                        .circleCrop()
                        .into(userImageTV)
            }

            designationTV.text = this.designation
            userNameTV.text = this.name
        }

        override fun onClick(v: View?) {
            otherOptionClickListener?.onPeopleToExpectClicked(gigPeopleToExpect[adapterPosition])
        }
    }

}