package com.gigforce.app.modules.preferences.daytime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

interface OnSlotClickListener {
    fun onItemChecked(which: Int, isChecked: Boolean)
}

class SlotsRecyclerAdapter : RecyclerView.Adapter<SlotsRecyclerAdapter.MyViewHolder>() {

    private var slotList: Array<String> = emptyArray()
    private var sectionSelection: BooleanArray = BooleanArray(1)

    private var onSlotClickListener: OnSlotClickListener? = null

    fun setOnTransactionClickListener(onSlotClickListener: OnSlotClickListener) {
        this.onSlotClickListener = onSlotClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_settings_slots_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindTo(slotList[position], sectionSelection.get(position))
    }

    override fun getItemCount(): Int {
        return slotList.size
    }

    fun updateSlots(slotList: Array<String>, sectionSelection: BooleanArray) {
        this.slotList = slotList
        this.sectionSelection = sectionSelection
        notifyDataSetChanged()
    }

    fun setItemChecked(position: Int, checked: Boolean) {
        sectionSelection[position] = checked
        notifyItemChanged(position)
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view),
        CompoundButton.OnCheckedChangeListener {

        private val checkBox: AppCompatCheckBox = view.findViewById(R.id.slotCheckBox)

        init {
            checkBox.setOnCheckedChangeListener(this)
        }

        fun bindTo(slot: String, shouldBeChecked: Boolean) {
            checkBox.text = slot
            checkBox.isChecked = shouldBeChecked
            checkBox.setPadding(10,0,0,0)
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            onSlotClickListener?.onItemChecked(adapterPosition, isChecked)
        }
    }
}