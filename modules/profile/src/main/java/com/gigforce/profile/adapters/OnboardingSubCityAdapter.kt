package com.gigforce.profile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.fragments.preferredJobLocation.OnboardingPreferredJobLocationFragment

class OnboardingSubCityAdapter(
        private val context: Context
) : RecyclerView.Adapter<OnboardingSubCityAdapter.OnboardingSubCityViewHolder>() {


    private var originalSubCityList: List<String> = emptyList()
    private var selectedItemIndex: Int = -1

    private var onSubCitySelectedListener: OnSubCitySelectedListener? = null

    fun setOnSubCitySelectedListener(onSubCitySelectedListener: OnboardingPreferredJobLocationFragment) {
        this.onSubCitySelectedListener = onSubCitySelectedListener
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OnboardingSubCityViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(R.layout.prefered_job_location_details_item, parent, false)
        return OnboardingSubCityViewHolder(view)
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
        return originalSubCityList.size
    }

    override fun onBindViewHolder(holder: OnboardingSubCityViewHolder, position: Int) {
        holder.bindValues(originalSubCityList.get(position), position)
    }

    fun setData(contacts: List<String>) {

        this.selectedItemIndex = -1
        this.originalSubCityList = contacts
        //this.filteredCityList = contacts
        notifyDataSetChanged()
    }


    inner class OnboardingSubCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private var subCityName: TextView = itemView.findViewById(R.id.sub_city_title)
        private var subCityCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            subCityCheckbox.setOnCheckedChangeListener(this@OnboardingSubCityViewHolder)
        }

        fun bindValues(subCity: String, position: Int) {
            subCityName.text = subCity

//            if (selectedItemIndex == position) {
//                cityNameTv.setTextColor(ResourcesCompat.getColor(context.resources, R.color.lipstick, null))
//                cityNameTv.setTypeface(null, Typeface.BOLD)
//            } else {
//                cityNameTv.setTextColor(ResourcesCompat.getColor(context.resources, R.color.black, null))
//                cityNameTv.setTypeface(null, Typeface.NORMAL)
//            }
        }

        override fun onClick(v: View?) {
            val subCity = originalSubCityList.get(adapterPosition)

            subCityCheckbox.performClick()
            if (subCityCheckbox.isChecked) {
                // add to list
                onSubCitySelectedListener?.onSubCitySelected(true, subCity)
            } else {
                //remove from list
                onSubCitySelectedListener?.onSubCitySelected(false, subCity)
            }
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val subCity = originalSubCityList.get(adapterPosition)

            if(isChecked){
                onSubCitySelectedListener?.onSubCitySelected(true, subCity)
            } else {
                onSubCitySelectedListener?.onSubCitySelected(true, subCity)
            }
        }

    }



}