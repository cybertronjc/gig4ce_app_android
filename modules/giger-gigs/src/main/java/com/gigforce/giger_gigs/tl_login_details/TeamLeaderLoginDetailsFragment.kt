package com.gigforce.giger_gigs.tl_login_details

import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.adapters.TLLoginSummaryAdapter
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.team_leader_login_details_fragment.*
import javax.inject.Inject


@AndroidEntryPoint
class TeamLeaderLoginDetailsFragment : Fragment(), OnTlItemSelectedListener {

    companion object {
        fun newInstance() = TeamLeaderLoginDetailsFragment()
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: TeamLeaderLoginDetailsViewModel
    private lateinit var viewBinding: TeamLeaderLoginDetailsFragmentBinding

    val PAGE_START = 1
    var currentPage = PAGE_START
    var isLoading = false
    var isLastPage = false
    var scrollingAdded = false

    private val tlLoginSummaryAdapter: TLLoginSummaryAdapter by lazy {
        TLLoginSummaryAdapter(requireContext(),this).apply {
            setOnTlItemSelectedListener(this@TeamLeaderLoginDetailsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = TeamLeaderLoginDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TeamLeaderLoginDetailsViewModel::class.java)

        //checkForAddUpdate()
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

//    private fun checkForAddUpdate() {
//        var navFragmentsData = activity as NavFragmentsData
//        if (navFragmentsData?.getData() != null) {
//            if (navFragmentsData?.getData()
//                    ?.getBoolean(LoginSummaryConstants.CAME_BACK_FROM_ADD, false) == true
//            ) {
//                didCamebackfromAdd = false
//                navFragmentsData?.setData(bundleOf())
//            }
//        }
//    }

    private val INTERVAL_TIME: Long = 1000 * 5
    private val SWIPE_INTERVAL_TIME: Long = 1000 * 1

    var hadler = Handler()
    var swipeToRefreshHandler = Handler()
    var runnable : Runnable? = null
    fun refreshListHandler() {
        runnable = Runnable{
            try {
                if (!onpaused && swipeToRefresh) {
                    initializeViews()
                }
                refreshListHandler()
            } catch (e: Exception) {

            }

        }
        hadler.postDelayed(runnable, INTERVAL_TIME)

    }

    fun stopSwipeToRefresh()
    {
        swipeToRefreshHandler.postDelayed({
            viewBinding.swipeRefresh?.isRefreshing = false
        },SWIPE_INTERVAL_TIME)
    }

    var onpaused = false
    override fun onPause() {
        super.onPause()
        onpaused = true
        runnable?.let {
            hadler.removeCallbacks(runnable)
        }

    }

    override fun onResume() {
        super.onResume()
        onpaused = false
        refreshListHandler()
    }

    private fun initToolbar() = viewBinding.apply {
        appBarComp.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

    }

    private fun initializeViews() = viewBinding.apply {
        //loadFirstPage
        currentPage = 1
        isLoading = false
        viewModel.getListingForTL(1)
    }


    private fun listeners() = viewBinding.apply {

        swipeRefresh.setOnRefreshListener {
            swipeToRefresh = true
            stopSwipeToRefresh()

        }

        addNew.setOnClickListener {
            navigation.navigateTo(
                "gig/addNewLoginSummary", bundleOf(
                    LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_ADD
                )
            )
        }

    }

    private fun observer() = viewBinding.apply {
        viewModel.loginListing.observe(viewLifecycleOwner, Observer {
            val res = it ?: return@Observer
            when (res) {
                Lce.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }

                is Lce.Content -> {
                    progressBar.visibility = View.GONE
                    progressBarBottom.visibility = View.GONE
                    setupReyclerView(res.content)
                }

                is Lce.Error -> {
                    showToast(getString(R.string.error_loading_data_giger_gigs))
                    progressBar.visibility = View.GONE
                }
            }
        })
    }

    var swipeToRefresh = true

    private fun setupReyclerView(res: List<ListingTLModel>)  = viewBinding.apply{

        if (res.isEmpty()) {
            noData.visibility = View.VISIBLE
            datecityRv.visibility = View.GONE
        } else {
            noData.visibility = View.GONE
            datecityRv.visibility = View.VISIBLE
        }
        val layoutManager = LinearLayoutManager(context)
        datecityRv.layoutManager = layoutManager
        if (currentPage == 1){
            Log.d("pag", "zero $currentPage, list : ${res.size}")
            tlLoginSummaryAdapter.submitList(res)
        }else {
            Log.d("pag", "nonzero $currentPage, list : ${res.size}" )
            tlLoginSummaryAdapter.updateList(res)
            tlLoginSummaryAdapter.notifyDataSetChanged()
            if (layoutManager.findLastVisibleItemPosition() >= 6 && layoutManager.findFirstVisibleItemPosition()<=6){
                scrollingAdded = false
            }

            //datecityRv.smoothScrollToPosition(tlLoginSummaryAdapter.itemCount/2)

        }
        datecityRv.adapter = tlLoginSummaryAdapter
        val totalPages = res.get(0).totalPages


        datecityRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isLoading = true
                }

            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //Log.d("Scrolled", "onScrolled $dx ,: $dy")

                val currentItemsLatest = layoutManager.childCount
                val totalItemsLatest = layoutManager.itemCount


                //Log.d("Scrolled", " isLoading: ${isLoading} , currentItemsLatest : $currentItemsLatest, lastVisibleItemPosition: $lastVisibleItemPosition, totalItemsLatest: $totalItemsLatest ")
                //if (isLoading && (currentItemsLatest + lastVisibleItemPosition == totalItemsLatest) && (totalItemsLatest <= tlLoginSummaryAdapter.itemCount)   ) {
                if ((currentPage < totalPages) && isLoading ){
                    //load next page
                    currentPage += 1
                    isLoading = false
                    scrollingAdded = true
                    swipeToRefresh = false
                    progressBarBottom.visibility = View.VISIBLE
                    viewModel.getListingForTL(currentPage)

                }

            }
        })

    }

    override fun onTlItemSelected(listingTLModel: ListingTLModel) {
        if (DateUtils.isToday(listingTLModel.dateTimestamp)) {
            navigation.navigateTo(
                "gig/addNewLoginSummary", bundleOf(
                    LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_EDIT,
                    LoginSummaryConstants.INTENT_LOGIN_SUMMARY to listingTLModel
                )
            )
        } else {
            navigation.navigateTo(
                "gig/addNewLoginSummary", bundleOf(
                    LoginSummaryConstants.INTENT_LOGIN_SUMMARY to listingTLModel,
                    LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_VIEW
                )
            )

        }
    }
}