package com.gigforce.app.modules.client_activation

import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.bullet_string_item.view.*
import kotlinx.android.synthetic.main.layout_rv_bullet_points.view.*

class AdapterBulletStrings : RecyclerView.Adapter<AdapterBulletStrings.ViewHolder>() {
    var items: List<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.bullet_string_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bulletPoint = items[position]
        holder.itemView.point.text = bulletPoint
        holder.itemView.bullet.setImageResource(R.drawable.shape_circle_lipstick)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    fun addData(items: List<String>) {
        this.items = items;
        notifyDataSetChanged()
    }
}