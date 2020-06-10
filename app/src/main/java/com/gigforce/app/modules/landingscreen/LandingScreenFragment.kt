package com.gigforce.app.modules.landingscreen

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet.UpcomingGigModel
import kotlinx.android.synthetic.main.explore_by_role_item.*
import kotlinx.android.synthetic.main.home_screen_bottom_sheet_fragment.*
import java.util.ArrayList

class LandingScreenFragment : Fragment() {

    companion object {
        fun newInstance() = LandingScreenFragment()
    }

    private lateinit var viewModel: LandingScreenViewModel
    var width: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landingscreen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandingScreenViewModel::class.java)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
//        initializeUpcomingGigBottomSheet()
    }
    private fun initializeUpcomingGigBottomSheet() {
        val itemWidth = ((width / 5) * 4).toInt()
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->

                },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    val lp = card_view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    card_view.layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_role_item)
        upcoming_gig_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        upcoming_gig_rv.adapter = recyclerGenericAdapter

    }
}