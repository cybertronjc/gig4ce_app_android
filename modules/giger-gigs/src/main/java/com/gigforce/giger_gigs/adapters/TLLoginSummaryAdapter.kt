package com.gigforce.giger_gigs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.utils.DateHelper
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.TeamLeaderLoginDetailsFragment
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import kotlinx.android.synthetic.main.date_city_recycler_item_layout.view.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class TLLoginSummaryAdapter(
    private val context: Context, private val teamLeaderLoginDetailsFragment: TeamLeaderLoginDetailsFragment
) : RecyclerView.Adapter<TLLoginSummaryAdapter.TlLoginViewHolder>() {

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
        private val gigerCount: TextView = view.gigerCount
        fun bindValues(listingTLModel: ListingTLModel, position: Int) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
                val date =  LocalDate.parse(listingTLModel.date, formatter)
                val actualDate = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                dateTV.text = DateHelper.getDateInDDMMMYYYY(actualDate)
            }catch (e: Exception){
                dateTV.text = listingTLModel.date
            }

            cityTV.text = listingTLModel.city.name

            var totalCount = 0
            listingTLModel.businessData.forEach {
                it.gigerCount?.let {
                    totalCount += it
                }
            }

            gigerCount.setText("$totalCount ${context.resources.getString(R.string.logins_gigs)}")

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

    fun updateList(list: List<ListingTLModel>){
        val tempList = filteredList.toMutableList()
        tempList.addAll(list)
        this.submitList(tempList)
//        filteredList = tempList

    }


    override fun getItemCount(): Int {
        return filteredList.size
    }
}