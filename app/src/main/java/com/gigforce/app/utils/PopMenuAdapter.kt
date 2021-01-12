package com.gigforce.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gigforce.app.R
import com.skydoves.powermenu.MenuBaseAdapter

class PopMenuAdapter : MenuBaseAdapter<MenuItem?>() {
    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
        var view: View? = view
        val context = viewGroup.context
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.pop_up_menu_tv, viewGroup, false)
        }

        if (index != itemList.size - 1) {
            view?.setPadding(
                0,
                view.context.resources.getDimensionPixelSize(R.dimen.size_16),
                0,
                0
            )

        } else {
            view?.setPadding(
                0,
                view.context.resources.getDimensionPixelSize(R.dimen.size_16),
                0,
                view.context.resources.getDimensionPixelSize(R.dimen.size_16)
            )
        }

        val (titleText) = getItem(index) as MenuItem
        val title = view?.findViewById<TextView>(R.id.tv_pop_menu_normal)
        title?.text = titleText
        return super.getView(index, view, viewGroup)
    }
}