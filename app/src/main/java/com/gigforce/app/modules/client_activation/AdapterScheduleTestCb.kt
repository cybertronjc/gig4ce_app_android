package com.gigforce.app.modules.client_activation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.client_activation.models.CheckItem
import kotlinx.android.synthetic.main.layout_rv_check_schedule_test.view.*

class AdapterScheduleTestCb : RecyclerView.Adapter<AdapterScheduleTestCb.ViewHolder>() {

    private lateinit var callbacks: AdapterScheduleTestCbCallbacks
    var items: List<CheckItem> = listOf()
    var disableItems = false
    var selectedItems: MutableList<CheckItem> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_check_schedule_test, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.cb_schedule_test.text = item.content
        if(disableItems)holder.itemView.cb_schedule_test.isEnabled = false
        holder.itemView.cb_schedule_test.setOnClickListener {
            if (holder.adapterPosition == -1) return@setOnClickListener
            items[holder.adapterPosition].isCheckedBoolean = holder.itemView.cb_schedule_test.isChecked
            callbacks.enableConfirmOtpButton(items.all { it.isCheckedBoolean })
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

    fun addData(items: List<CheckItem>) {
        selectedItems.clear()
        this.items = items;
        notifyDataSetChanged()
    }
    fun disableAllItems(){
        disableItems = true
        notifyDataSetChanged()
    }
    fun setCallbacks(callbacks: AdapterScheduleTestCbCallbacks) {
        this.callbacks = callbacks
    }

    interface AdapterScheduleTestCbCallbacks {
        fun enableConfirmOtpButton(enable: Boolean)
    }


}