package com.gigforce.app.modules.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet.UpcomingGigModel
import kotlinx.android.synthetic.main.fragment_main_learning.*
import kotlinx.android.synthetic.main.fragment_main_learning_recent_video_item.view.*
import java.util.ArrayList


class MainLearningFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_main_learning, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        dummLayout1.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout1.videoDescTV.text = "Industry Based"
        dummLayout1.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning))

        dummLayout2.videoTitleTV.text = "How to apply for driving license?"
        dummLayout2.videoDescTV.text = "Role Based"
        dummLayout2.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning2))

        dummLayout3.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout3.videoDescTV.text = "Industry Based"
        dummLayout3.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning1))

        initializeExploreByIndustry()
        mostPopularLearning()

    }

    private fun mostPopularLearning() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 2.8) * 1).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())

        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->

                },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_industry_item)
        mostPopularLearningsRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mostPopularLearningsRV.adapter = recyclerGenericAdapter
    }

    var width: Int = 0
    private fun initializeExploreByIndustry() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->

                },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_industry_item)
        searchSuggestionBasedVideosRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        searchSuggestionBasedVideosRV.adapter = recyclerGenericAdapter
    }
}