package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import com.gigforce.client_activation.client_activation.models.City
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.core.utils.GlideApp
import kotlinx.android.synthetic.main.layout_rv_role_details.view.*

class ActiveLocationsAdapter(
    private val context: Context
) : RecyclerView.Adapter<ActiveLocationsAdapter.ViewHolder>() {

    private var items= ArrayList<City>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_role_details, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (items != null) items?.size!! else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.city_name_tv.text = items.get(position).name ?: ""
        GlideApp.with(context).load(items.get(position).image).into(holder.itemView.city_image_iv)

    }

    fun addData(items: ArrayList<City>) {
        this.items = items
        notifyDataSetChanged()
    }
}