package com.gigforce.profile.onboarding.fragments.assetsowned

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
import com.gigforce.profile.onboarding.fragments.interest.AllInterestAdapter
import com.gigforce.profile.onboarding.fragments.interest.InterestDM
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import kotlinx.android.synthetic.main.image_text_item_view.view.*

class AssetAdapter(
    val context: Context,
    val allInterestList: ArrayList<AssestDM>,
    val onAssestClickListener: OnAssestClickListener,
    val assetOwnedFragment: AssetOwnedFragment
) :
    RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    var adapter: AssetAdapter? = null

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssetAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_text_item_view, parent, false)
        if (adapter == null) adapter = this
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: AssetAdapter.ViewHolder, position: Int) {
        holder.bindItems(allInterestList.get(position), position)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        Log.d("item count", "" + allInterestList.size)
        return allInterestList.size
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(assetDM: AssestDM, position: Int) {
            val icon = itemView.icon_iv as ImageView
            val interestName = itemView.interest_name as TextView
            if (assetDM.icon.isNotEmpty()) {
                Glide.with(context).load(assetDM.icon).into(icon)
            } else {
                icon.setImageResource(assetOwnedFragment.getAssetLocalIcon(assetDM.name))
            }
            interestName.setText(assetDM.name)

            itemView.setOnClickListener({
                onAssestClickListener.onclick(it, position)
            })
        }


    }

    interface OnAssestClickListener {
        fun onclick(view: View, position: Int)
    }
}
