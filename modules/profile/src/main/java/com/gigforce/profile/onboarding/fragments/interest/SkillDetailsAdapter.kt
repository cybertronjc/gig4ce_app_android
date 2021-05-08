package com.gigforce.profile.onboarding.fragments.interest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.profile.R
import com.gigforce.profile.models.SkillsDetails
import kotlinx.android.synthetic.main.image_text_item_view.view.*

class SkillDetailsAdapter(
    val context: Context,
    val allSkillDetailsList: List<SkillsDetails>,
    val onDeliveryExecutiveClickListener: OnDeliveryExecutiveClickListener,
    val interestFragment: InterestFragment
) :
    RecyclerView.Adapter<SkillDetailsAdapter.ViewHolder>() {
    var adapter: SkillDetailsAdapter? = null

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SkillDetailsAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_text_item_view, parent, false)
        if (adapter == null) adapter = this
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: SkillDetailsAdapter.ViewHolder, position: Int) {
        holder.bindItems(allSkillDetailsList.get(position), position)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return allSkillDetailsList.size
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(interestDM: SkillsDetails, position: Int) {
            Log.d("interest DM", interestDM.toString())
            val icon = itemView.icon_iv as ImageView
            val interestName = itemView.interest_name as TextView
            if (interestDM.icon.isNotEmpty()) {
                Glide.with(context).load(interestDM.icon).into(icon)
            }
            else{
                icon.setImageResource(interestFragment.getSkillLocalIcon(interestDM.name))
            }
            interestName.setText(interestDM.name)

            itemView.setOnClickListener({
                onDeliveryExecutiveClickListener.onclick(it,position)
            })
        }


    }

    interface OnDeliveryExecutiveClickListener {
        fun onclick(view: View, position: Int)
    }
}