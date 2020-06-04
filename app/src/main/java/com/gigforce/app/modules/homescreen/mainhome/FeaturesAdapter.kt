package com.gigforce.app.modules.homescreen.mainhome

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment

internal class FeaturesAdapter internal constructor(val baseFragment: BaseFragment, private val resource: Int, private val itemList: Array<String>?) : ArrayAdapter<FeaturesAdapter.ItemHolder>(
    baseFragment.requireContext(), resource) {

    override fun getCount(): Int {
        return if (this.itemList != null) this.itemList.size else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val holder: ItemHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, null)
            holder =
                ItemHolder()
            holder.name = convertView!!.findViewById(R.id.txt_title_hs1)
            holder.icon = convertView.findViewById(R.id.img_icon_hs1)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ItemHolder
        }
        holder.icon?.setOnClickListener{
            if (position == 1) {
                baseFragment.navigate(R.id.walletBalancePage)
            }
            else if(position==4){
                baseFragment.navigate(R.id.profileFragment)
            }
            else if(position==5){
                baseFragment.navigate(R.id.settingFragment)
            }
            else if(position==7){
                baseFragment.navigate(R.id.videoResumeFragment)
            }
        }
        holder.name!!.text = this.itemList!![position]
        when (position) {
            0 -> {holder.icon!!.setImageResource(R.drawable.mygig)
                holder.name!!.text = "My Gig"}
            1 -> {
                holder.icon!!.setImageResource(R.drawable.wallet)
                holder.name!!.text = "Wallet"}
            2 -> {holder.icon!!.setImageResource(R.drawable.learning)
                holder.name!!.text = "Learning"}
            3 -> {holder.icon!!.setImageResource(R.drawable.explore_hs_features)
                holder.name!!.text = "Explore"}
            4 -> {holder.icon!!.setImageResource(R.drawable.profile)
                holder.name!!.text = "Profile"}
            5 -> {holder.icon!!.setImageResource(R.drawable.settings)
                holder.name!!.text = "Setting"}
            6 -> {holder.icon!!.setImageResource(R.drawable.chat)
                holder.name!!.text = "Chat"}
            7 -> {holder.icon!!.setImageResource(R.drawable.gig4ce_logo)
                holder.name!!.text = "Video Resume"}
            else -> { // Note the block
                holder.icon!!.setImageResource(R.drawable.gig4ce_logo)
                holder.name!!.text = "More"
            }
        }

        return convertView
    }

    internal class ItemHolder {
        var name: TextView? = null
        var icon: ImageView? = null
    }
}