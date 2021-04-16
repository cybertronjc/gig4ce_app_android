package com.gigforce.profile.adapters

import android.content.Context
import android.graphics.Typeface
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

class OnboardingSubCityAdapter(
        private val context: Context
) : RecyclerView.Adapter<OnboardingSubCityAdapter.OnboardingSubCityViewHolder>(){


    private var originalSubCityList: List<String> = emptyList()
    private var selectedItemIndex: Int = -1
//    private var onCitySelectedListener : OnCitySelectedListener? = null
//
//    fun setOnCitySelectedListener(onCitySelectedListener: OnCitySelectedListener){
//        this.onCitySelectedListener = onCitySelectedListener
//    }

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
            View.OnClickListener {

        private var subCityName: TextView = itemView.findViewById(R.id.sub_city_title)

        init {
            itemView.setOnClickListener(this)
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

//            val city = filteredCityList[newPosition]
//            onCitySelectedListener?.onCitySelected(city)
        }

    }

}