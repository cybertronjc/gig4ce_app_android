package com.gigforce.profile.adapters

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.profile.R
import com.gigforce.profile.models.City

class OnboardingCityAdapter(
        private val context: Context
) : RecyclerView.Adapter<OnboardingCityAdapter.OnboardingCityViewHolder>(),
        Filterable {

    private var originalCityList: List<City> = emptyList()
    private var filteredCityList: List<City> = emptyList()

    private val contactsFilter = CityFilter()

    private var selectedItemIndex: Int = -1
    private var onCitySelectedListener : OnCitySelectedListener? = null

    fun setOnCitySelectedListener(onCitySelectedListener: OnCitySelectedListener){
        this.onCitySelectedListener = onCitySelectedListener
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OnboardingCityViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(R.layout.recycler_item_city, parent, false)
        return OnboardingCityViewHolder(view)
    }

    fun getSelectedItemIndex(): Int {
        return selectedItemIndex
    }

    fun resetSelectedItem() {
        if (selectedItemIndex == -1)
            return

        val tempIndex = selectedItemIndex
        selectedItemIndex = -1
        notifyItemChanged(tempIndex)
    }

    override fun getItemCount(): Int {
        return filteredCityList.size
    }

    override fun onBindViewHolder(holder: OnboardingCityViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION
                &&  position < filteredCityList.size
        ) {
            holder.bindValues(filteredCityList.get(position), position)
        }
    }

    fun setData(contacts: List<City>) {

        this.selectedItemIndex = -1
        this.originalCityList = contacts
        this.filteredCityList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filteredList: MutableList<City> = mutableListOf()
            if (charString.isEmpty()) {
                filteredList.addAll(originalCityList)
            } else {

                for (contact in originalCityList) {
                    if (contact.name.contains(
                                    charString,
                                    true
                            )
                    )
                    filteredList.add(contact)
                }
                Log.d("FilteredL","${filteredList.size}")
            }

            val filterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCityList = results!!.values as List<City>
            notifyDataSetChanged()
        }
    }


    inner class OnboardingCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private var cityNameTv: TextView = itemView.findViewById(R.id.city_name_tv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(city: City, position: Int) {
            cityNameTv.text = city.name

            if (selectedItemIndex == position) {
                cityNameTv.setTextColor(ResourcesCompat.getColor(context.resources, R.color.lipstick, null))
                cityNameTv.setTypeface(null,Typeface.BOLD)
            } else {
                cityNameTv.setTextColor(ResourcesCompat.getColor(context.resources, R.color.black, null))
                cityNameTv.setTypeface(null,Typeface.NORMAL)
            }
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition

            if (selectedItemIndex != -1) {
                val tempIndex = selectedItemIndex
                selectedItemIndex = newPosition
                notifyItemChanged(tempIndex)
                notifyItemChanged(selectedItemIndex)
            } else {
                selectedItemIndex = newPosition
                notifyItemChanged(selectedItemIndex)
            }

            val city = filteredCityList[newPosition]
            onCitySelectedListener?.onCitySelected(city,false)
        }

    }

    fun uncheckedSelection(){
        if(selectedItemIndex!=-1){
            var position = selectedItemIndex
            selectedItemIndex = -1
            notifyItemChanged(position)
        }
    }

}