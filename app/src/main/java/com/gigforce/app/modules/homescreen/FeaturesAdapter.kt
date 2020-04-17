package com.gigforce.app.modules.homescreen

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.app.R

internal class FeaturesAdapter internal constructor(context: Context, private val resource: Int, private val itemList: Array<String>?) : ArrayAdapter<FeaturesAdapter.ItemHolder>(context, resource) {

    override fun getCount(): Int {
        return if (this.itemList != null) this.itemList.size else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val holder: ItemHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, null)
            holder = ItemHolder()
            holder.name = convertView!!.findViewById(R.id.txt_title_hs1)
            holder.icon = convertView.findViewById(R.id.img_icon_hs1)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ItemHolder
        }

        holder.name!!.text = this.itemList!![position]
        when (position) {
            0 -> {holder.icon!!.setImageResource(R.drawable.mygig)
                holder.name!!.text = "Profile"}
            1 -> {
                holder.icon!!.setImageResource(R.drawable.wallet)
                holder.name!!.text = "Learning"}
            2 -> {holder.icon!!.setImageResource(R.drawable.learning)
                holder.name!!.text = "Payment"}
            3 -> {holder.icon!!.setImageResource(R.drawable.explore_hs_features)
                holder.name!!.text = "Search"}
            4 -> {holder.icon!!.setImageResource(R.drawable.profile)
                holder.name!!.text = "Chat"}
            5 -> {holder.icon!!.setImageResource(R.drawable.settings)
                holder.name!!.text = "Support"}
            6 -> {holder.icon!!.setImageResource(R.drawable.chat)
                holder.name!!.text = "Preferences"}
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