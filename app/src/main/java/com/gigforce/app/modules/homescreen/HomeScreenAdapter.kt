package com.gigforce.app.modules.homescreen
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.app.R

internal class HomeScreenAdapter internal constructor(context: Context, private val resource: Int, private val itemList: Array<String>?) : ArrayAdapter<HomeScreenAdapter.ItemHolder>(context, resource) {

    override fun getCount(): Int {
        return if (this.itemList != null) this.itemList.size else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val holder: ItemHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, null)
            holder = ItemHolder()
            holder.name = convertView!!.findViewById(R.id.textView)
            holder.icon = convertView.findViewById(R.id.icon)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ItemHolder
        }

        holder.name!!.text = this.itemList!![position]
        when (position) {
            0 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_profile)
                holder.name!!.text = "Profile"}
            1 -> {
                holder.icon!!.setImageResource(R.drawable.ic_homescreen_learn)
                holder.name!!.text = "Learning"}
            2 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_payment)
                holder.name!!.text = "Payment"}
            3 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_explore)
                holder.name!!.text = "Search"}
            4 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_chat)
                holder.name!!.text = "Chat"}
            5 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_pref)
                holder.name!!.text = "Support"}
            6 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_control)
                holder.name!!.text = "Preferences"}
            7 -> {holder.icon!!.setImageResource(R.drawable.gig4ce_logo)
                holder.name!!.text = "Video Resume"}
            8 -> {holder.icon!!.setImageResource(R.drawable.ic_homescreen_explore)
                holder.name!!.text = "New HomeScreen"}
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