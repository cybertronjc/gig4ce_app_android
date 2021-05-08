package com.gigforce.profile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.profile.R
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage
import com.gigforce.profile.onboarding.fragments.preferredJobLocation.OnboardingPreferredJobLocationFragment

class OnboardingMajorCityAdapter(
        private val context: Context,
        private val requestManager: RequestManager,
        private val onboardingPreferredJobLocationFragment: OnboardingPreferredJobLocationFragment
) : RecyclerView.Adapter<OnboardingMajorCityAdapter.OnboardingMajorCityViewHolder>(),
        Filterable {

    private var isUserGroupManager: Boolean = false

    private var originalCityList= ArrayList<CityWithImage>()
    private var filteredCityList= ArrayList<CityWithImage>()

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
        holder.bindValues(filteredCityList.get(position), position)
    }

    fun setData(contacts: ArrayList<CityWithImage>) {

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
                val filteredList = ArrayList<CityWithImage>()
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
            filteredCityList = results?.values as ArrayList<CityWithImage>
            notifyDataSetChanged()
        }
    }


    inner class OnboardingMajorCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private var cityNameTv: TextView = itemView.findViewById(R.id.city_name_tv)
        private var cityImageIV: ImageView = itemView.findViewById(R.id.city_image_iv)
        private var cityRootLayout: LinearLayout = itemView.findViewById(R.id.city_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(city: CityWithImage, position: Int) {
            if (city.icon.isNotEmpty()){
                requestManager.load(city.image).into(cityImageIV)
            }
            else{
                cityImageIV.setImageResource(onboardingPreferredJobLocationFragment.getMajorCityIcon(city.name))
            }

            cityNameTv.text = city.name

            if (selectedItemIndex == position) {
                cityImageIV.setColorFilter(
                        ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                )
                cityRootLayout.setBackgroundDrawable(
                                ContextCompat.getDrawable(
                                        context,
                                        R.drawable.option_selection_border
                                )
                        )
                cityNameTv.setTextColor(ContextCompat.getColor(context, R.color.selected_text_color))
//                cityRootLayout.setBackgroundResource(R.drawable.rectangle_round_light_pink)

            } else {
                cityImageIV.setColorFilter(null)
                cityRootLayout.setBackgroundResource(R.drawable.rectangle_round_light_blue)
                cityNameTv.setTextColor(ContextCompat.getColor(context, R.color.default_color))

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
//            if (selectedItemIndex != -1) {
//                selectedItemIndex = -1
//            } else {
//                selectedItemIndex = adapterPosition
//            }
//            notifyDataSetChanged()

            val city = filteredCityList[newPosition]
            onCitySelectedListener?.onCitySelected(
                    City(
                            id = city.id,
                            name = city.name,
                            stateCode = city.stateCode
                    ),true
            )
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