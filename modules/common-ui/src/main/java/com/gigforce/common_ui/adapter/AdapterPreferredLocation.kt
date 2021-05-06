package com.gigforce.common_ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.R
import kotlinx.android.synthetic.main.layout_rv_role_details.view.*

class AdapterPreferredLocation : RecyclerView.Adapter<AdapterPreferredLocation.ViewHolder>() {

    private var items: List<String?>? = null

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
        holder.itemView.tv_rv_preferred_location.text = items?.get(position) ?: ""

    }

    fun addData(items: List<String?>) {
        this.items = items
        notifyDataSetChanged()
    }
}