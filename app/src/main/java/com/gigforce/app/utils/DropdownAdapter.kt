package com.gigforce.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.gigforce.app.R
import kotlinx.android.synthetic.main.dropdown_spinner.view.*

/*
Adapter class made to the design consideration for app dropdown elements.
This adapter should be used for making drop downs throughout the app.
*/
class DropdownAdapter(context: Context, array: List<String>):
    ArrayAdapter<String>(context, 0, array) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return this.createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return this.createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val value = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.dropdown_spinner, parent, false)

        view.item.text = value
        return view
    }
}