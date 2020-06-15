package com.gigforce.app.modules.landingscreen

import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet.UpcomingGigModel
import kotlinx.android.synthetic.main.landingscreen_fragment.*
import java.util.ArrayList

class LandingScreenFragment : BaseFragment() {

    companion object {
        fun newInstance() = LandingScreenFragment()
    }

    private lateinit var viewModel: LandingScreenViewModel
    var width: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.landingscreen_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandingScreenViewModel::class.java)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        initializeGigforceTip()
        initializeExploreByRole()
        initializeExploreByIndustry()
        initializeLearningModule()
        listener()
    }

    class GigforceTips(var title: String, var subtitle: String) {

    }

    private val SPLASH_TIME_OUT: Long = 2000 // 1 sec
    var forward = true
    private fun initializeGigforceTip() {
        // model will change when integrated with DB
        var datalist: ArrayList<GigforceTips> = ArrayList<GigforceTips>()
        datalist.add(
            GigforceTips(
                "Gigforce Tip ",
                "Having  an experience can help you start earning fast"
            )
        )
        datalist.add(
            GigforceTips(
                "Gigforce Tip ",
                "Having  an experience can help you start earning fast"
            )
        )
        datalist.add(
            GigforceTips(
                "Gigforce Tip ",
                "Having  an experience can help you start earning fast"
            )
        )
        datalist.add(
            GigforceTips(
                "Gigforce Tip ",
                "Having  an experience can help you start earning fast"
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<GigforceTips> =
            RecyclerGenericAdapter<GigforceTips>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.explore_by_role)
                },
                RecyclerGenericAdapter.ItemInterface<GigforceTips?> { obj, viewHolder, position ->
                    var title = getTextView(viewHolder, R.id.gigtip_title)
                    var subtitle = getTextView(viewHolder, R.id.gigtip_subtitle)

                    val lp = title.layoutParams
                    lp.height = lp.height
                    lp.width = width
                    title.layoutParams = lp
                    title.text = obj?.title
                    subtitle.text = obj?.subtitle
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.gigforce_tips_item)
        gigforce_tip.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        gigforce_tip.adapter = recyclerGenericAdapter
        var pagerHelper = PagerSnapHelper()
        pagerHelper.attachToRecyclerView(gigforce_tip)
        var handler = Handler()
//        val runnable = Runnable {
//            var currentVisiblePosition = (gigforce_tip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//            gigforce_tip.scrollToPosition(currentVisiblePosition+1)
//            handler.postDelayed(runnable,SPLASH_TIME_OUT)
//        }

        val runnableCode = object : Runnable {
            override fun run() {
                try {
                    var currentVisiblePosition =
                        (gigforce_tip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if ((gigforce_tip.adapter as RecyclerGenericAdapter<GigforceTips>).list.size == currentVisiblePosition + 1) {
                        forward = false
                    }
                    if (currentVisiblePosition == 0) {
                        forward = true
                    }
                    if (!forward) {
                        gigforce_tip.smoothScrollToPosition(currentVisiblePosition - 1)
                    } else
                        gigforce_tip.smoothScrollToPosition(currentVisiblePosition + 1)


                    handler.postDelayed(this, SPLASH_TIME_OUT)
                }catch (e:Exception){

                }

            }
        }
        handler.postDelayed(runnableCode, SPLASH_TIME_OUT)
    }

    private fun listener() {
        complete_now.setOnClickListener {
            navigate(R.id.gigerVerificationFragment)
        }
        view_my_gig.setOnClickListener {
            navigate(R.id.mainHomeScreen)
        }
        skip_about_intro.setOnClickListener {
            about_us_cl.visibility = View.GONE
        }
    }

    private fun initializeLearningModule() {

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
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_rv.adapter = recyclerGenericAdapter
    }

    private fun initializeExploreByIndustry() {

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
        explore_by_industry.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_by_industry.adapter = recyclerGenericAdapter
    }

    private fun initializeExploreByRole() {
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.explore_by_role)
                },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_role_item)
        explore_by_role_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_by_role_rv.adapter = recyclerGenericAdapter

    }
}