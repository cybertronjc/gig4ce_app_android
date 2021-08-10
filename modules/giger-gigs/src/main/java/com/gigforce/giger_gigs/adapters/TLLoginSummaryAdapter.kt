package com.gigforce.giger_gigs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.TeamLeaderLoginDetailsFragment
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import kotlinx.android.synthetic.main.date_city_recycler_item_layout.view.*


class TLLoginSummaryAdapter() : RecyclerView.Adapter<TLLoginSummaryAdapter.TlLoginViewHolder>() {

     var originalList: List<ListingTLModel> = emptyList()
     var filteredList: List<ListingTLModel> = emptyList()
    private var selectedItemIndex: Int = -1

    private var onTlItemSelectedListener : OnTlItemSelectedListener? = null

    fun setOnTlItemSelectedListener(onTlItemSelectedListener: OnTlItemSelectedListener){
        this.onTlItemSelectedListener = onTlItemSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TlLoginViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.date_city_recycler_item_layout, parent, false)
        return TlLoginViewHolder(view)
    }
    override fun onBindViewHolder(holder: TlLoginViewHolder, position: Int) {
        holder.bindValues(filteredList.get(position), position)
    }

    inner class TlLoginViewHolder(view: View) : RecyclerView.ViewHolder(
         view
    ), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        private val dateTV: TextView = view.dateTV
        private val cityTV: TextView = view.cityTV
        fun bindValues(listingTLModel: ListingTLModel, position: Int) {
            dateTV.text = listingTLModel.date
            cityTV.text = listingTLModel.city.name
        }

        override fun onClick(p0: View?) {
            val newPosition = adapterPosition
            val listingTlModel = filteredList[newPosition]
            onTlItemSelectedListener?.onTlItemSelected(listingTlModel)

        }

    }

    fun getSelectedItemIndex(): Int {
        return selectedItemIndex
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