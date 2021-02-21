package com.gigforce.app.modules.learning.myLearning.assessment

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet.BSCalendarScreenFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_my_assessments.*
import java.util.ArrayList


class MyAssessmentsInnerFragment : Fragment() {


    private val viewModelProfile: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_my_assessments, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initPendingAss()

    }

    private fun initCompletedAss() {

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val itemWidth = ((width / 5) * 3.5).toInt()

        val datalist: ArrayList<BSCalendarScreenFragment.Assessment> = ArrayList<BSCalendarScreenFragment.Assessment>()

        datalist.add(
            BSCalendarScreenFragment.Assessment(
                "Influence",
                "02:00 Min",
                true
            )
        )
        datalist.add(
            BSCalendarScreenFragment.Assessment(
                "Negotiation",
                "05:00 Min",
                false
            )
        )


        val recyclerGenericAdapter: RecyclerGenericAdapter<BSCalendarScreenFragment.Assessment> =
                RecyclerGenericAdapter<BSCalendarScreenFragment.Assessment>(
                        activity?.applicationContext,
                        PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                            showToast("This page are inactive. We’ll activate it in a few weeks")
                            //navigate(R.id.assessment_fragment)
                        },
                        RecyclerGenericAdapter.ItemInterface<BSCalendarScreenFragment.Assessment?> { obj, viewHolder, position ->
                            val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
                            lp.height = lp.height
                            lp.width = itemWidth
                            getView(viewHolder, R.id.assessment_cl).layoutParams = lp
                            getTextView(viewHolder, R.id.title).text = obj?.title
                            getTextView(viewHolder, R.id.time).text = obj?.time


                            getTextView(viewHolder, R.id.status).text = "PENDING"
                            getTextView(
                                    viewHolder,
                                    R.id.status
                            ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
                            (getView(
                                    viewHolder,
                                    R.id.side_bar_status
                            ) as ImageView).setImageResource(R.drawable.assessment_line_pending)


                        })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
        suggested_assessment_recycler_view.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        suggested_assessment_recycler_view.adapter = recyclerGenericAdapter
       
    }

    private fun initPendingAss() {

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val itemWidth = ((width / 5) * 3.5).toInt()

        var datalist: ArrayList<BSCalendarScreenFragment.Assessment> = ArrayList<BSCalendarScreenFragment.Assessment>()

        datalist.add(
            BSCalendarScreenFragment.Assessment(
                "Influence",
                "02:00 Min",
                true
            )
        )
        datalist.add(
            BSCalendarScreenFragment.Assessment(
                "Negotiation",
                "05:00 Min",
                false
            )
        )


        val recyclerGenericAdapter: RecyclerGenericAdapter<BSCalendarScreenFragment.Assessment> =
            RecyclerGenericAdapter<BSCalendarScreenFragment.Assessment>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    showToast("This page are inactive. We’ll activate it in a few weeks")
                    //navigate(R.id.assessment_fragment)
                },
                RecyclerGenericAdapter.ItemInterface<BSCalendarScreenFragment.Assessment?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.assessment_cl).layoutParams = lp
                    getTextView(viewHolder, R.id.title).text = obj?.title
                    getTextView(viewHolder, R.id.time).text = obj?.time


                    getTextView(viewHolder, R.id.status).text = "PENDING"
                    getTextView(
                            viewHolder,
                            R.id.status
                    ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
                    (getView(
                        viewHolder,
                        R.id.side_bar_status
                    ) as ImageView).setImageResource(R.drawable.assessment_line_pending)


                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
        suggested_assessment_recycler_view.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        suggested_assessment_recycler_view.adapter = recyclerGenericAdapter
    }

}