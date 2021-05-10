package com.gigforce.profile.onboarding.fragments.interest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.profile.R
import kotlinx.android.synthetic.main.image_text_item_view.view.*

class AllInterestAdapter(
    val context: Context,
    val allInterestList: ArrayList<InterestDM>,
    val onDeliveryExecutiveClickListener: OnDeliveryExecutiveClickListener
) :
    RecyclerView.Adapter<AllInterestAdapter.ViewHolder>() {
    var adapter: AllInterestAdapter? = null

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllInterestAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_text_item_view, parent, false)
        if (adapter == null) adapter = this
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: AllInterestAdapter.ViewHolder, position: Int) {
        holder.bindItems(allInterestList.get(position), position)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        Log.d("item count", ""+allInterestList.size)
        return allInterestList.size
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(interestDM: InterestDM, position: Int) {
            Log.d("interest DM", interestDM.toString())
            val icon = itemView.icon_iv as ImageView
            val interestName = itemView.interest_name as TextView
            if (interestDM.icon.isNotEmpty()) {
                Glide.with(context).load(interestDM.icon).into(icon)
            }
            else{
                icon.setImageResource(getSkillLocalIcon(interestDM.skill))
            }
            interestName.setText(interestDM.skill)

            itemView.setOnClickListener({
                onDeliveryExecutiveClickListener.onclick(it,position)
            })
        }


    }

    interface OnDeliveryExecutiveClickListener {
        fun onclick(view:View,position: Int)
    }

    fun getSkillLocalIcon(name: String) : Int{
        var icon = R.drawable.ic_driving_wheel
        var map = mapOf<String, Int>("Driving" to R.drawable.ic_driving_wheel,
                "Delivery Executive" to R.drawable.ic_delivery_truck,
                "Sales" to R.drawable.ic_sale,
                "Technician" to R.drawable.ic_technician,
                "Helper" to R.drawable.ic_trolley,
                "Security" to R.drawable.ic_security,
                "Tele Calling" to R.drawable.ic_technician,
                "Supervisor" to R.drawable.ic_supervisor,
                "Cleaner" to R.drawable.ic_cleaning,
                "Farmers" to R.drawable.ic_plant_in_hand,)

        if (map.containsKey(name)){
            icon = map.get(name)!!
        }
        return icon
    }
}