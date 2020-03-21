package com.gigforce.app.modules.homescreen

//import com.gigforce.app.modules.onboarding.CustomAdapter
//import com.gigforce.app.modules.onboarding.models.MessageModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.bottom_home.*
import kotlinx.android.synthetic.main.bottom_home.view.*
import kotlinx.android.synthetic.main.fragment_roaster.*
import kotlinx.android.synthetic.main.item_grid_action.*
import kotlinx.android.synthetic.main.item_grid_action.view.*

//import kotlinx.android.synthetic.main.fragment_roaster.buttonCP

class HomeScreenFragment() : Fragment() {

    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout =  inflater.inflate(R.layout.layout_home_screen, container, false)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.gridview_actions.adapter = GridActionItemsAdapter(context!!)

        button_tmp.setOnClickListener{
            findNavController().navigate(R.id.profileFragment)
        }
        //this.gridview_actions.adapter.getView(1,layout, layout.parent as ViewGroup?).img_icon.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        //layout.gridview_actions[1]?.findViewById<ImageView>(R.id.img_icon).setOnClickListener { findNavController().navigate(R.id.profileFragment) }
//        Log.d(">>>>>>>>>>",this.gridview_actions[0].toString())
        //layout?.gridview_actions[0]?.img_icon.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        //Log.d(">>>>>>>>>>",this.gridview_actions.adapter.getItemId(1).toString());


//            view!!.findViewById<ImageView>(R.id.img_icon).setOnClickListener {findNavController().navigate(R.id.profileFragment)}

//        this.gridview_actions.adapter.getView(1,this.view, this.view?.parent as ViewGroup?).setOnClickListener {
//            findNavController().navigate(R.id.profileFragment)
//        }
            //.setOnClickListener { findNavController().navigate(R.id.profileFragment) }


        // Populate dummy messages in List, you can implement your code here
        // Populate dummy messages in List, you can implement your code here
  /*      val messagesList: ArrayList<MessageModel> = ArrayList()
        for (i in 0..9) {
            messagesList.add(
                MessageModel(
                    "Hi",
                    if (i % 2 == 0) CustomAdapter.MESSAGE_TYPE_IN else CustomAdapter.MESSAGE_TYPE_OUT
                )
            )
        }

        val adapter = CustomAdapter(this, messagesList)

        recyclerView = findViewById(android.R.id.recycler_view)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter
        */


//
//        buttonCP?.setOnClickListener {
//            //findNavController().navigate(R.id.gotoOB)
//            findNavController().navigate(R.id.createInitProfile)
////            Toast.makeText(activity,"captured click",Toast.LENGTH_SHORT).show();
////            childFragmentManager.beginTransaction().apply {
////                add(R.id.content_home, UserInfoFragment())
////                addToBackStack(null).commit()
////            }
//        }
//
//        sliderAdaptorButton?.setOnClickListener {
//            //findNavController().navigate(R.id.goToOBIntroFragment)
////            Toast.makeText(activity,"captured click",Toast.LENGTH_SHORT).show();
////            childFragmentManager.beginTransaction().apply {
////                add(R.id.content_home, UserInfoFragment())
////                addToBackStack(null).commit()
////            }
//        }
    }

    class GridActionItemsAdapter(val context: Context) : BaseAdapter() {
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