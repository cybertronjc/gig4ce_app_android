package com.gigforce.app.modules.roaster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.modules.home.HomeFragment
import com.gigforce.app.modules.photocrop.*
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
//         get reference to button
         val cta = view.findViewById(R.id.cta) as AppCompatButton
        // set on-click listener
        cta.setOnClickListener {
            // your code to perform when the user clicks on the button
//            Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
            val intent_pc = Intent(context, PhotoCrop::class.java)
            getActivity()?.startActivity(intent_pc)
        }
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

            Glide.with(context)
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