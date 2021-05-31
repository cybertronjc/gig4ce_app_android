package com.gigforce.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.gigforce.app.R

class GenericSpinnerAdapter<T>(context: Context, resource: Int, objects: List<T>) :
    ArrayAdapter<T>(context, resource, objects) {
    private var items: List<T>? = null
    var layout = 0
    fun getItems(): List<T>? {
        return items
    }

    fun setItems(items: List<T>?) {
        this.items = items
        notifyDataSetChanged()
    }

    private fun init(objects: List<T>, resource: Int) {
        items = objects
        layout = resource
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val customView = LayoutInflater.from(context).inflate(layout, parent, false)
        val tv = customView as TextView

        return super.getView(position, tv, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val tv = v as TextView
        if (position == 0) {
            tv.height = 0
        } else {
            tv.text = items!![position].toString()

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