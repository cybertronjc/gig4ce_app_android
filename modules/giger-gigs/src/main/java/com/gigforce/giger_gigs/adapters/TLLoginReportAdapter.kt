package com.gigforce.giger_gigs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.models.DailyLoginReport
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.TeamLeaderLoginDetailsFragment
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import kotlinx.android.synthetic.main.recycler_item_daily_report_item.view.*


interface OnTlReportItemSelectedListener {
    fun onTlReportSelected(
        listingTLModel: DailyLoginReport
    )
}

class TLLoginReportAdapter() : RecyclerView.Adapter<TLLoginReportAdapter.TlLoginViewHolder>() {

     var originalList: List<DailyLoginReport> = emptyList()
     var filteredList: List<DailyLoginReport> = emptyList()
    private var selectedItemIndex: Int = -1

    private var onTlItemSelectedListener : OnTlReportItemSelectedListener? = null

    fun setOnTlItemSelectedListener(onTlItemSelectedListener: OnTlReportItemSelectedListener){
        this.onTlItemSelectedListener = onTlItemSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TlLoginViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.recycler_item_daily_report_item, parent, false)
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

        private val businessAndCityTV: TextView = view.businessAndcityTV
        private val profileNameTV: TextView = view.profile_name_tv
        fun bindValues(listingTLModel: DailyLoginReport, position: Int) {
            profileNameTV.text = listingTLModel.businessData?.businessName
            businessAndCityTV.text = "${listingTLModel.businessData?.jobProfileName} - ${listingTLModel.businessData?.city?.name}"
        }

        override fun onClick(p0: View?) {
            val newPosition = adapterPosition
            val listingTlModel = filteredList[newPosition]
            onTlItemSelectedListener?.onTlReportSelected(listingTlModel)

        }

    }

    fun getSelectedItemIndex(): Int {
        return selectedItemIndex
    }

    fun submitList(contacts: List<DailyLoginReport>) {

        this.originalList = contacts
        this.filteredList = contacts
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }
}