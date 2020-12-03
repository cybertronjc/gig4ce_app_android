package com.gigforce.app.modules.client_activation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.layout_rv_check_schedule_test.view.*

class AdapterScheduleTestCb : RecyclerView.Adapter<AdapterScheduleTestCb.ViewHolder>() {

    var items: List<String> = listOf()

    var selectedItems: MutableList<String> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_check_schedule_test, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.cb_schedule_test.text = item
        holder.itemView.cb_schedule_test.setOnClickListener {
            if (holder.adapterPosition == -1) return@setOnClickListener
            if (holder.itemView.cb_schedule_test.isChecked) {
                selectedItems.add(items[holder.adapterPosition])
            } else {
                selectedItems.remove(items[holder.adapterPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addData(items: List<String>) {
        selectedItems.clear()
        this.items = items;
        notifyDataSetChanged()
    }


}