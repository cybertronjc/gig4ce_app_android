package com.gigforce.app.modules.homescreen

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_home_screen.*


class HomeScreenIcons : Fragment() {

    //todo
    private lateinit var storage: FirebaseStorage

    private lateinit var layout: View

    private val itemList: Array<String>
        get() = arrayOf("Profile", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12")


    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val window: Window = activity!!.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(activity!!.resources.getColor(R.color.colorAccent))

        layout = inflater.inflate(R.layout.layout_home_screen, container, false)

        return layout
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gridview = layout.findViewById<GridView>(R.id.gridview)

        val adapter = this.context?.let { HomeScreenAdapter(it, R.layout.item_gridhomescreen, itemList) }
        gridview.adapter = adapter

        //topbar.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        cardviewkyc.setOnClickListener { Toast.makeText(context, "TODO CTA: jump to kyc docs upload page", Toast.LENGTH_SHORT).show() }
        cardviewvideo.setOnClickListener { Toast.makeText(context, "TODO CTA: jump to video resume upload page", Toast.LENGTH_SHORT).show() }

        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // Write code to perform action when item is clicked.

            when (position) {
                0 -> {findNavController().navigate(R.id.profileFragment)}
                1 -> {
                //    findNavController().navigate(R.id.profileFragment)
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    //findNavController().navigate(R.id.profileFragment)
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                7 -> {
                    findNavController().navigate(R.id.videoResumeFragment)
                    //Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "TODO CTA: $position", Toast.LENGTH_SHORT).show()
                }
            }

            if(position==0){

            }
        }
    }
}