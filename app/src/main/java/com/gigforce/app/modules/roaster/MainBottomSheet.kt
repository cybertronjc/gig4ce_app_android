package com.gigforce.app.modules.roaster

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.bottom_home.view.*

class MainBottomSheet(context: Context, attributeSet: AttributeSet):
    LinearLayout(context, attributeSet)
{
    fun setDetaults() {
        this.orientation = LinearLayout.VERTICAL
    }

    init {
        setDetaults()
        LayoutInflater.from(context).inflate(R.layout.bottom_home, this, true)
        this.gridview_actions.adapter = GridActionItemsAdapter(context)
    }

}

class GridActionItemsAdapter(val context: Context): BaseAdapter() {

    override fun getCount(): Int {
        return 15
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView;
        view ?: let {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_grid_action, parent, false)
        }

        GlideApp.with(context)
            .load("")
            .placeholder(R.drawable.placeholder_user)
            .into(view!!.findViewById<ImageView>(R.id.img_icon))

        return view!!;
    }

    override fun getItem(position: Int): Any {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}