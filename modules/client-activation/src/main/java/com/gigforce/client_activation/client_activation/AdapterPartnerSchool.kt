package com.gigforce.client_activation.client_activation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.models.PartnerSchoolDetails
import com.gigforce.core.extensions.*
import kotlinx.android.synthetic.main.layout_rv_partner_school_address.view.*

class AdapterPartnerSchool : RecyclerView.Adapter<AdapterPartnerSchool.ViewHolder>() {

    private lateinit var callbacks: AdapterPartnerSchoolCallbacks
    var items: List<PartnerSchoolDetails> = arrayListOf()
    var selectedItem = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_partner_school_address, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partner = items[position]
        if (partner.name != null) {
            holder.itemView.tv_name_partner_school.visible()
            holder.itemView.tv_name_partner_school.text = partner.name
        } else {
            holder.itemView.tv_name_partner_school.gone()
        }
        if (partner.line1 != null) {
            holder.itemView.tv_landmark_partner_school.visible()
            holder.itemView.tv_landmark_partner_school.text = partner.line1
        } else {
            holder.itemView.tv_landmark_partner_school.gone()
        }
        if (partner.line2 != null) {
            holder.itemView.tv_city_partner_school.visible()
            holder.itemView.tv_city_partner_school.text = partner.line2
        } else {
            holder.itemView.tv_city_partner_school.gone()
        }
        if (partner.line3 != null) {
            holder.itemView.tv_timing_partner_school.visible()
            holder.itemView.tv_timing_partner_school.text = partner.line3
        } else {
            holder.itemView.tv_timing_partner_school.gone()
        }
        holder.itemView.tv_owner_partner_school.text =
            partner.contact.map {
                it.name
            }.reduce { acc, item -> "$acc,$item" }
        holder.itemView.parent_partner_school_rv.setBackgroundResource(if (selectedItem == position) R.drawable.selected_item_partner_school else R.drawable.border_979797_1dp_rad4)
        holder.itemView.setOnClickListener {
            selectedItem = holder.adapterPosition
            notifyDataSetChanged()
            callbacks.onItemClick(holder.adapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return items.size;
    }

    fun addData(items: List<PartnerSchoolDetails>) {
        this.items = items;
        notifyDataSetChanged()
    }

    fun getSelectedItem(): PartnerSchoolDetails {
        return items[selectedItem]
    }

    fun setCallbacks(callbacks: AdapterPartnerSchoolCallbacks) {
        this.callbacks = callbacks;
    }


    interface AdapterPartnerSchoolCallbacks {
        fun onItemClick(position: Int);

    }


}