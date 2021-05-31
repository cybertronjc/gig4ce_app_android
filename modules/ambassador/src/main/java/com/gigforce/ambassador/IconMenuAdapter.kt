package com.gigforce.ambassador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.common_ui.IconPowerMenuItem
import com.skydoves.powermenu.MenuBaseAdapter

class IconMenuAdapter : MenuBaseAdapter<IconPowerMenuItem?>() {
    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
        var view: View? = view
        val context = viewGroup.context
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.layout_pop_up_menu_item, viewGroup, false)
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

        val (icon1, title1) = getItem(index) as IconPowerMenuItem
        val icon = view!!.findViewById<ImageView>(R.id.iv_pop_up_menu)
        icon.setImageDrawable(icon1)
        val title = view.findViewById<TextView>(R.id.tv_pop_up_menu)
        title.text = title1
        return super.getView(index, view, viewGroup)
    }
}