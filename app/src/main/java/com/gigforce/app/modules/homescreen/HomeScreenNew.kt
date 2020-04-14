package com.gigforce.app.modules.homescreen

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
        lateinit var title:String;
        lateinit var subTitle:String;
        lateinit var date:String;
        lateinit var comment:String;
        lateinit var month:String;

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var datalist: ArrayList<DataItem> = ArrayList<DataItem>()
        datalist.add(DataItem())

        val recyclerGenericAdapter: RecyclerGenericAdapter<DataItem> =
            RecyclerGenericAdapter<DataItem>(
                activity.applicationContext,
                OnViewHolderClick<Any?> { view, position, item -> showToast("")},
                ItemInterface<DataItem?> { obj, viewHolder ->

                })
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.home_screen_new_fragment)
        rvHS1.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        rvHS1.setAdapter(recyclerGenericAdapter)
    }

}