package com.gigforce.giger_gigs.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.models.ListingTLModel
import kotlinx.android.synthetic.main.date_city_recycler_item_layout.view.*


class TLLoginSummaryAdapter : RecyclerView.Adapter<TLLoginSummaryAdapter.TlLoginViewHolder>() {

    private var originalList: List<ListingTLModel> = emptyList()
    private var filteredList: List<ListingTLModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TlLoginViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.date_city_recycler_item_layout, parent, false)
        return TlLoginViewHolder(view)
    }
    override fun onBindViewHolder(holder: TlLoginViewHolder, position: Int) {
        holder.bindValues(filteredList.get(position), position)
    }
    class TlLoginViewHolder(view: View) : RecyclerView.ViewHolder(
         view
    ) {

        private val dateTV: TextView = view.dateTV
        private val cityTV: TextView = view.cityTV
        fun bindValues(listingTLModel: ListingTLModel, position: Int) {
            dateTV.text = listingTLModel.date
            cityTV.text = listingTLModel.city.name
        }

    }

    fun submitList(contacts: List<ListingTLModel>) {

        this.originalList = contacts
        this.filteredList = contacts
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }
}