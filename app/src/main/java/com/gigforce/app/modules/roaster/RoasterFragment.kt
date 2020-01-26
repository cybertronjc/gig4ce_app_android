package com.gigforce.app.modules.roaster

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.modules.home.HomeFragment
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.bottom_home.*

class RoasterFragment(): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_roaster, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.gridview_actions.adapter = GridActionItemsAdapter(context!!)
    }

    class GridActionItemsAdapter(val context: Context): BaseAdapter() {

        override fun getCount(): Int {
            return 10
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

        override fun getItem(p0: Int): Any {
            return 1
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

    }
}