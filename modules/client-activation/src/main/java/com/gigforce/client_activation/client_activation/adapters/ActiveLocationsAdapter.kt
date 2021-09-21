package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import com.gigforce.core.datamodels.client_activation.City
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
class ActiveLocationsAdapter(
    private val context: Context
) : RecyclerView.Adapter<ActiveLocationsAdapter.LocationViewHolder>(){


    private var originalJobList: List<City> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.active_location_item_view, parent, false)
        return LocationViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalJobList.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bindValues(originalJobList.get(position), position)
    }

    fun setData(contacts: List<City>) {
        this.originalJobList = contacts
        notifyDataSetChanged()
    }

    inner class LocationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var jobTitleTv: TextView = itemView.findViewById(R.id.city_name_tv)
        private var jobImage: ImageView = itemView.findViewById(R.id.city_image_iv)


        fun bindValues(jobProfile: City, position: Int) {
            jobTitleTv.text = jobProfile.name
            Glide.with(context).load(jobProfile.image).into(jobImage)

        }

    }

}