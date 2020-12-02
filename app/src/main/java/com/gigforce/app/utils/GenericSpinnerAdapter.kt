package com.gigforce.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.gigforce.app.R

class GenericSpinnerAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String?>(context, resource, objects) {
    private var items: List<String>? = null
    var layout = 0
    fun getItems(): List<String>? {
        return items
    }

    fun setItems(items: List<String>?) {
        this.items = items
        notifyDataSetChanged()
    }

    private fun init(objects: List<String>, resource: Int) {
        items = objects
        layout = resource
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val customView = LayoutInflater.from(context).inflate(layout, parent, false)
        val tv = customView as TextView
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0)
        tv.text = items!![position]
        return super.getView(position, tv, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.layout_custom_spinner_drop_down, parent, false)
        val tv = v as TextView
        if (position == 0) {
            tv.height = 0
        } else {
            tv.text = items!![position]
        }
        if (parent != null && parent is ListView) {
            var lv: ListView? = null
            try {
                lv = parent
            } catch (e: Exception) {
            }
            if (lv != null) {
                lv.divider = null
            }
        }
        return super.getDropDownView(position, v, parent)
    }

    init {
        init(objects, resource)
    }
}