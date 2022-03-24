package com.gigforce.lead_management.ui.other_cities

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.OtherCityClusterItem
import com.gigforce.lead_management.R

class SelectOtherCitiesAdapter (
    private val context: Context,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable {

    companion object {
        const val ALPHABET_VIEW = 1
        const val CITY_VIEW = 2
    }

    private var originalCityList = ArrayList<OtherCityClusterItem>()
    private var filteredCityList = ArrayList<OtherCityClusterItem>()
    private var selectedOtherCitiesList: ArrayList<OtherCityClusterItem> = arrayListOf()

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
    ): RecyclerView.ViewHolder {
        if (viewType == ALPHABET_VIEW){
            return HeaderViewHolder(LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.recycler_item_alphabet_city_layout,
                parent,
                false
            ))
        }
        return CityViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.recycler_item_city_name_layout,
                parent,
                false
            )
        )

    }

    fun getSelectedOtherCities(): ArrayList<OtherCityClusterItem>? {
        if (selectedOtherCitiesList == null)
            return null
        else {
            return selectedOtherCitiesList
        }
    }

    override fun getItemCount(): Int {
        return filteredCityList.size
    }

    override fun getItemViewType(position: Int): Int {
        return originalCityList[position].viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (originalCityList[position].viewType === ALPHABET_VIEW) {
            (holder as HeaderViewHolder).bindValues(originalCityList[position])
        } else {
            (holder as CityViewHolder).bindValues(originalCityList[position], position)
        }
    }

    fun setData(contacts: ArrayList<OtherCityClusterItem>) {

        val preSelectedItems = contacts.filter {
            it.selected
        }
        if(preSelectedItems.isNotEmpty()){
            this.selectedOtherCitiesList?.addAll(preSelectedItems)
        } else {
            this.selectedOtherCitiesList = arrayListOf()
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
                Log.d("SearchText", "${constraint.toString()}")
            } else {
                val filteredList = ArrayList<OtherCityClusterItem>()
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
            if (constraint.toString().isNotEmpty()) {
                filteredCityList.clear()
                val updatedFilteredList = results?.values as ArrayList<OtherCityClusterItem>
                val groupedList = updatedFilteredList.groupBy { it.name?.get(0) }
                groupedList.forEach { (alphabet, otherCities) ->
                    filteredCityList.add(
                        OtherCityClusterItem(alphabet.toString(), "", false, 1)
                    )
                    otherCities.forEach {
                        it.viewType = 2
                        filteredCityList.add(
                            it
                        )
                    }
                }
                //filteredCityList = results?.values as ArrayList<OtherCityClusterItem>
//                Log.d("SearchText", "${constraint.toString()}")
//                if (constraint.toString().isEmpty()) {
//                    filteredCityList = originalCityList
//                    Log.d("SearchTextFiltered", "${filteredCityList} , original: $originalCityList")
//                    this@SelectOtherCitiesAdapter.setData(originalCityList)
//                } else {
//                    Log.d(
//                        "SearchTextFilteredNotEmpty",
//                        "${filteredCityList} , original: $originalCityList"
//                    )
//                    this@SelectOtherCitiesAdapter.setData(filteredCityList)
//                }
                this@SelectOtherCitiesAdapter.setData(filteredCityList)
                notifyDataSetChanged()

                onCitySelectedListener?.onCityFiltered(
                    cityCountVisibleAfterFiltering = filteredCityList.size,
                    selectedCityVisible = filteredCityList.find {
                        selectedOtherCitiesList.contains(
                            it
                        )
                    } != null
                )
            }
        }
    }


    inner class CityViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var cityNameTv: TextView = itemView.findViewById(R.id.cityname_text)
        private var selectedCheckBox: CheckBox = itemView.findViewById(R.id.city_checkbox)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(city: OtherCityClusterItem, position: Int) {

            if (city.name?.isNotBlank() == true){
                cityNameTv.text = city.name
            }

            selectedCheckBox.isChecked = city.selected
            selectedCheckBox.setOnClickListener {
                val newPosition = adapterPosition
                val city = filteredCityList[newPosition]

                if (selectedOtherCitiesList != null && selectedOtherCitiesList?.contains(city) == true) {
                    city.selected = false
                    selectedOtherCitiesList?.remove(city)
                } else {
                    city.selected = true
                    selectedOtherCitiesList?.add(city)
                }
                onCitySelectedListener?.onCitySelected(
                    city
                )
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition
            val city = filteredCityList[newPosition]
            if (selectedOtherCitiesList != null && selectedOtherCitiesList?.contains(city) == true) {
                    city.selected = false
                selectedCheckBox.isChecked = false
                selectedOtherCitiesList?.remove(city)
            } else {
                city.selected = true
                selectedCheckBox.isChecked = true
                selectedOtherCitiesList?.add(city)
            }

            onCitySelectedListener?.onCitySelected(
                city
            )
        }
    }

    inner class HeaderViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var alphabetText: TextView = itemView.findViewById(R.id.alphabet_text)


        fun bindValues(alphabet: OtherCityClusterItem) {
            alphabetText.setText(alphabet.name)
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

    fun getIndexFromIdOriginalList(
        id: String
    ): Int {
        val city = originalCityList.find { it.id == id }

        return if (city == null)
            return -1
        else
            originalCityList.indexOf(city)
    }


    interface OnCitySelectedListener {

        fun onCityFiltered(
            cityCountVisibleAfterFiltering : Int,
            selectedCityVisible : Boolean
        )

        fun onCitySelected(selectedCity: OtherCityClusterItem)
    }


}