package com.gigforce.profile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.profile.R
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage

class OnboardingMajorCityAdapter(
        private val requestManager: RequestManager
) : RecyclerView.Adapter<OnboardingMajorCityAdapter.OnboardingMajorCityViewHolder>(),
        Filterable {

    private var isUserGroupManager: Boolean = false

    private var originalCityList: List<CityWithImage> = emptyList()
    private var filteredCityList: List<CityWithImage> = emptyList()

    private val contactsFilter = CityFilter()

    private var selectedItemIndex: Int = -1
    private var onCitySelectedListener: OnCitySelectedListener? = null

    fun setOnCitySelectedListener(onCitySelectedListener: OnCitySelectedListener) {
        this.onCitySelectedListener = onCitySelectedListener
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OnboardingMajorCityViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(R.layout.recycler_item_major_city, parent, false)
        return OnboardingMajorCityViewHolder(view)
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

    override fun onBindViewHolder(holder: OnboardingMajorCityViewHolder, position: Int) {
        holder.bindValues(filteredCityList.get(position))
    }

    fun setData(contacts: List<CityWithImage>) {

        this.selectedItemIndex = -1
        this.originalCityList = contacts
        this.filteredCityList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                filteredCityList = originalCityList
            } else {
                val filteredList: MutableList<CityWithImage> = mutableListOf()
                for (contact in originalCityList) {
                    if (contact.name.contains(
                                    charString,
                                    true
                            )
                    )
                        filteredList.add(contact)
                }
                filteredCityList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = filteredCityList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCityList = results?.values as List<CityWithImage>
            notifyDataSetChanged()
        }
    }


    inner class OnboardingMajorCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private var cityNameTv: TextView = itemView.findViewById(R.id.city_name_tv)
        private var cityImageIV: ImageView = itemView.findViewById(R.id.city_image_iv)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(city: CityWithImage) {
            requestManager.load(city.image).into(cityImageIV)
            cityNameTv.text = city.name
        }

        override fun onClick(v: View?) {

            val city = filteredCityList[adapterPosition]
            onCitySelectedListener?.onCitySelected(
                    City(
                            id = city.id,
                            name = city.name,
                            stateCode = city.stateCode
                    )
            )
//            if (selectedItemIndex != -1) {
//                selectedItemIndex = -1
//            } else {
//                selectedItemIndex = adapterPosition
//            }
//            notifyDataSetChanged()
        }

    }

}