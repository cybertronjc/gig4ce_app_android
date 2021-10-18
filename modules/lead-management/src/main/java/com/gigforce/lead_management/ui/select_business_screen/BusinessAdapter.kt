package com.gigforce.lead_management.ui.select_business_screen

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
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.lead_management.R

class BusinessAdapter(
        private val context: Context,
        private val requestManager: RequestManager
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>(),
        Filterable {

    private var originalBusinessList= ArrayList<JoiningBusinessAndJobProfilesItem>()
    private var filteredBusinessList= ArrayList<JoiningBusinessAndJobProfilesItem>()

    private val contactsFilter = CityFilter()

    private var selectedId: String? = null
    //    private var selectedItemIndex: Int = -1

    private var onBusinesssSelectedListener: OnBusinessSelectedListener? = null

    fun setOnBusinessSelectedListener(onCitySelectedListener: OnBusinessSelectedListener) {
        this.onBusinesssSelectedListener = onCitySelectedListener
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BusinessViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(
            R.layout.recycler_item_business,
            parent,
            false
        )
        return BusinessViewHolder(view)
    }

    fun getSelectedBusiness() : JoiningBusinessAndJobProfilesItem?{
        if (selectedId == null)
            return null
        else {
            return originalBusinessList[getIndexFromIdFromOriginalList(selectedId!!)]
        }
    }

    override fun getItemCount(): Int {
        return filteredBusinessList.size
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        if(position != RecyclerView.NO_POSITION
                && position < filteredBusinessList.size
        ) {
            holder.bindValues(filteredBusinessList.get(position), position)
        }
    }

    fun setData(contacts: ArrayList<JoiningBusinessAndJobProfilesItem>) {

        val preSelectedItems = contacts.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }

        this.originalBusinessList = contacts
        this.filteredBusinessList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values  = originalBusinessList
            } else {
                val filteredList = ArrayList<JoiningBusinessAndJobProfilesItem>()
                for (contact in originalBusinessList) {
                    if (contact.name?.contains(
                            charString,
                            true
                        ) == true
                    )
                        filteredList.add(contact)
                }
                filterResults.values  = filteredList
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredBusinessList = results?.values as ArrayList<JoiningBusinessAndJobProfilesItem>
            notifyDataSetChanged()

            onBusinesssSelectedListener?.onBusinessFiltered(
                businessCountVisibleAfterFiltering =  filteredBusinessList.size,
                selectedBusinessVisible = filteredBusinessList.find { it.id == selectedId } != null
            )
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

        fun bindValues(business: JoiningBusinessAndJobProfilesItem, position: Int) {
            if (business.icon.isNullOrEmpty()){

                val companyInitials = if (business.name.isNullOrBlank())
                    "C"
                else
                    business.name!![0].toString().toUpperCase()

                val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                )
                businessImageIV.setImageDrawable(drawable)
            }
            else{
                requestManager.load(business.icon).into(businessImageIV)
            }

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
            val city = filteredBusinessList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = city.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = city.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onBusinesssSelectedListener?.onBusinessSelected(
                city
            )
        }

    }



    fun getIndexFromId(
        id: String
    ): Int {
        val city = filteredBusinessList.find { it.id == id }

        return if (city == null)
            return -1
        else
            filteredBusinessList.indexOf(city)
    }

    fun getIndexFromIdFromOriginalList(
        id: String
    ): Int {
        val city = originalBusinessList.find { it.id == id }

        return if (city == null)
            return -1
        else
            originalBusinessList.indexOf(city)
    }



    interface OnBusinessSelectedListener{

        fun onBusinessFiltered(
            businessCountVisibleAfterFiltering : Int,
            selectedBusinessVisible : Boolean
        )

        fun onBusinessSelected(businessSelected : JoiningBusinessAndJobProfilesItem)
    }

}