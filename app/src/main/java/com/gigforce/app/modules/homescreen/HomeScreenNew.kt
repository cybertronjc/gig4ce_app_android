package com.gigforce.app.modules.homescreen

import android.icu.text.CaseMap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter.OnViewHolderClick
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter.ItemInterface
import kotlinx.android.synthetic.main.homescreen_1.*


class HomeScreenNew : BaseFragment() {

    companion object {
        fun newInstance() = HomeScreenNew()
    }

    private lateinit var viewModel: HomeScreenNewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.homescreen_1, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeScreenNewViewModel::class.java)


        // TODO: Use the ViewModel
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
        var datalist: ArrayList<DataItem> = ArrayList<DataItem>()
        datalist.add(DataItem())

        val recyclerGenericAdapter: RecyclerGenericAdapter<DataItem> =
            RecyclerGenericAdapter<DataItem>(
                activity?.applicationContext,
                OnViewHolderClick<Any?> { view, position, item -> showToast("")},
                ItemInterface<DataItem?> { obj, viewHolder ->

                })
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.home_screen_new_fragment)
        rv_.setLayoutManager(
            LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        rv_.adapter = recyclerGenericAdapter
    }

}