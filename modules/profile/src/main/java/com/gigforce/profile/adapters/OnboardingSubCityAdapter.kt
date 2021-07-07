package com.gigforce.profile.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.profile.R
import com.gigforce.profile.models.SubCity
import com.gigforce.profile.onboarding.fragments.preferredJobLocation.OnboardingPreferredJobLocationFragment

class OnboardingSubCityAdapter(
        private val context: Context
) : RecyclerView.Adapter<OnboardingSubCityAdapter.OnboardingSubCityViewHolder>() {


    private var selectedSubCityList: List<String> = emptyList()
    private var originalSubCityList: List<SubCity> = emptyList()

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


    override fun getItemCount(): Int {
        return originalSubCityList.size
    }

    override fun onBindViewHolder(holder: OnboardingSubCityViewHolder, position: Int) {
        holder.bindValues(originalSubCityList.get(position), position)
    }

    fun setData(contacts: List<SubCity>, selectedCityList: List<String>) {

        this.originalSubCityList = contacts
        this.selectedSubCityList = selectedCityList
        notifyDataSetChanged()
    }


    inner class OnboardingSubCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {

        //private var subCityName: TextView = itemView.findViewById(R.id.sub_city_title)
        private var subCityCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
//            itemView.setOnClickListener(this)
            subCityCheckbox.setOnCheckedChangeListener(this@OnboardingSubCityViewHolder)
        }

        fun bindValues(subCity: SubCity, position: Int) {
            subCityCheckbox.text = subCity.name
            var found = false
            if (selectedSubCityList.size > 0) {
                found = selectedSubCityList.contains(subCity.name)
            }
            subCityCheckbox.isChecked = found
        }

//        override fun onClick(v: View?) {
//            val subCity = originalSubCityList.get(adapterPosition)
//
//            subCityCheckbox.performClick()
//            onSubCitySelectedListener?.onSubCitySelected(subCityCheckbox.isChecked, subCity)
//        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val subCity = originalSubCityList.get(adapterPosition)
            if (selectedSubCityList.size == 3 && isChecked){
                subCityCheckbox.isChecked = false
                Toast.makeText(context, "Maximum three localities can be selected!!", Toast.LENGTH_SHORT).show()
            } else {
                onSubCitySelectedListener?.onSubCitySelected(isChecked, subCity.name)
            }

        }

    }

}