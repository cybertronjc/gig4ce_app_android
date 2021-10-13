package com.gigforce.lead_management.ui.select_city

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.ReportingLocationsItem
import com.gigforce.lead_management.R

class CityAdapter(
    private val context: Context,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<CityAdapter.BusinessViewHolder>(),
    Filterable {

    private var originalCityList = ArrayList<ReportingLocationsItem>()
    private var filteredCityList = ArrayList<ReportingLocationsItem>()

    private val contactsFilter = CityFilter()

    private var selectedId: String? = null
//    private var selectedItemIndex: Int = -1
    private var onCitySelectedListener: OnCitySelectedListener? = null

    fun setOnCitySelectedListener(onCitySelectedListener: OnCitySelectedListener) {
        this.onCitySelectedListener = onCitySelectedListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusinessViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.recycler_item_city,
            parent,
            false
        )
        return BusinessViewHolder(view)
    }

    fun getSelectedCity(): ReportingLocationsItem? {
        if (selectedId == null)
            return null
        else {
            return filteredCityList.getOrNull(getIndexFromId(selectedId!!))
        }
    }

    override fun getItemCount(): Int {
        return filteredCityList.size
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        if (position != RecyclerView.NO_POSITION
            && position < filteredCityList.size
        ) {
            holder.bindValues(filteredCityList.get(position), position)
        }
    }

    fun setData(contacts: ArrayList<ReportingLocationsItem>) {

        val preSelectedItems = contacts.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }
        this.originalCityList = contacts
        this.filteredCityList = contacts


        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values = originalCityList
            } else {
                val filteredList = ArrayList<ReportingLocationsItem>()
                for (contact in originalCityList) {
                    if (contact.name?.contains(
                            charString,
                            true
                        ) == true
                    )
                        filteredList.add(contact)
                }
                filterResults.values = filteredList
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCityList = results?.values as ArrayList<ReportingLocationsItem>
            notifyDataSetChanged()
        }
    }


    inner class BusinessViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var businessNameTv: TextView = itemView.findViewById(R.id.city_name_tv)
        private var businessImageIV: ImageView = itemView.findViewById(R.id.city_image_iv)
        private var businessRootLayout: LinearLayout = itemView.findViewById(R.id.city_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(business: ReportingLocationsItem, position: Int) {

            val companyInitials = if (business.name.isNullOrBlank())
                "C"
            else
                business.name!![0].toString().toUpperCase()

            val drawable = TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
            )
            businessImageIV.setImageDrawable(drawable)
//            }
//            else{
//                requestManager.load(business.icon).into(businessImageIV)
//            }

            businessNameTv.text = business.name

            if (selectedId == business.id) {
                businessRootLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.option_selection_border
                )
            } else {
                businessRootLayout.setBackgroundResource(R.drawable.rectangle_2)
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition
            val city = filteredCityList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = city.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = city.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onCitySelectedListener?.onCitySelected(
                city
            )
        }
    }

    fun getIndexFromId(
        id: String
    ): Int {
        val city = filteredCityList.find { it.id == id }

        return if (city == null)
            return -1
        else
            filteredCityList.indexOf(city)
    }


    interface OnCitySelectedListener {

        fun onCitySelected(selectedCity: ReportingLocationsItem)
    }

}