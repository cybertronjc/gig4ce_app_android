package com.gigforce.app.modules.homescreen

import android.graphics.Color
import android.icu.text.CaseMap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter.OnViewHolderClick
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter.ItemInterface
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.homescreen1_bs1.*
import kotlinx.android.synthetic.main.homescreen_1.*
import kotlinx.android.synthetic.main.homescreen_1nsvbs.*

class DemoBottomSheetFragment : SuperBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.homescreen1_bs1, container, false)
    }
    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
    override fun getStatusBarColor() = Color.RED
}

class ExtBottomSheetFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.homescreen_1nsvbs, container, false)
    }
    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
    //override fun getStatusBarColor() = Color.RED
}

class HomeScreenNew : BaseFragment() {

    companion object {
        fun newInstance() = HomeScreenNew()
    }

    private lateinit var viewModel: HomeScreenNewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.homescreen_1nsvbs, inflater, container)
    }

    class DataItem{
        var title:String = "Title";
        var subTitle:String = "Sub Title";
        var date:String = "15";
        var comment:String = "below comments";
        var month:String = "April";

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val sheet = DemoBottomSheetFragment()
        //sheet.show(activity!!.supportFragmentManager, "bsFragment")

        val nsv = findViewById(R.id.nsv) as NestedScrollView // ye null hi aayega kyonki xml me nsv naam ka koi viewwww
        val extendedBottomSheetBehavior = ExtendedBottomSheetBehavior.from(nsv)
        //@ExtendedBottomSheetBehavior.State
        extendedBottomSheetBehavior.state = STATE_COLLAPSED
        val allowUserDragging:Boolean = true;
        extendedBottomSheetBehavior.isAllowUserDragging = allowUserDragging;


        viewModel = ViewModelProviders.of(this).get(HomeScreenNewViewModel::class.java)
        var datalist: ArrayList<DataItem> = ArrayList<DataItem>()
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())

        val recyclerGenericAdapter: RecyclerGenericAdapter<DataItem> =
                RecyclerGenericAdapter<DataItem>(
                        activity?.applicationContext,
                        OnViewHolderClick<Any?> { view, position, item -> showToast("")},
                        ItemInterface<DataItem?> { obj, viewHolder ->

                        })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.item_homescreen1)
        rv_.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
        )
        rv_.adapter = recyclerGenericAdapter
    }

}