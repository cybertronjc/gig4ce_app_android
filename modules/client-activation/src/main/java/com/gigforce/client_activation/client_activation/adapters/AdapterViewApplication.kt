package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.models.City
import com.gigforce.client_activation.client_activation.models.RequiredFeatures

class AdapterViewApplication(
    private val context: Context
) : RecyclerView.Adapter<AdapterViewApplication.ViewApplicationViewHolder>(){

    private var originalJobList: List<RequiredFeatures> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewApplicationViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.active_location_item_view, parent, false)
        return ViewApplicationViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalJobList.size
    }

    override fun onBindViewHolder(holder: ViewApplicationViewHolder, position: Int) {
        holder.bindValues(originalJobList.get(position), position)
    }

    fun setData(contacts: List<RequiredFeatures>) {
        this.originalJobList = contacts
        notifyDataSetChanged()
    }

    inner class ViewApplicationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var jobTitleTv: TextView = itemView.findViewById(R.id.tv_view_application)
        private var jobImage: ImageView = itemView.findViewById(R.id.iv_next_screen_application)


        fun bindValues(jobProfile: RequiredFeatures, position: Int) {
            jobTitleTv.text = jobProfile.title

        }

    }

}