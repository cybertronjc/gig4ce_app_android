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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseFragment2
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
class TeamLeaderLoginDetailsFragment : BaseFragment2<TeamLeaderLoginDetailsFragmentBinding>(
    fragmentName = "TeamLeaderLoginDetailsFragment",
    layoutId = R.layout.team_leader_login_details_fragment,
    statusBarColor = R.color.white
), OnTlItemSelectedListener, IOnBackPressedOverride {

    companion object {
        fun newInstance() = TeamLeaderLoginDetailsFragment()
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: TeamLeaderLoginDetailsViewModel by viewModels()
    private val loginSummarySharedViewModel : LoginSummarySharedViewModel by activityViewModels()

    val PAGE_START = 1
    var currentPage = PAGE_START
    var isLoading = false
    var isLastPage = false
    var scrollingAdded = false
    lateinit var layoutManager : LinearLayoutManager
    var tlListing = ArrayList<ListingTLModel>()
    var cameFromDeeplink = false

    private val tlLoginSummaryAdapter: TLLoginSummaryAdapter by lazy {
        TLLoginSummaryAdapter(requireContext(),this).apply {
            setOnTlItemSelectedListener(this@TeamLeaderLoginDetailsFragment)
        }
    }

    override fun viewCreated(
        viewBinding: TeamLeaderLoginDetailsFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        //checkForAddUpdate()
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initToolbar()
        initializeViews()
        observer()
        initSharedViewModel()
        listeners()
    }
    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            cameFromDeeplink = it.getBoolean(StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value) ?: return@let
        }
        savedInstanceState?.let {
            cameFromDeeplink = it.getBoolean(StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value) ?: return@let
        }

    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value, cameFromDeeplink)

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
        tlListing.clear()

        tlLoginSummaryAdapter.submitList(emptyList())
        tlLoginSummaryAdapter.notifyDataSetChanged()
        viewModel.getListingForTL(1)
    }


    private fun listeners() = viewBinding.apply {

        swipeRefresh.setOnRefreshListener {
            clearExistingListAndRefreshData()
        }

        addNew.setOnClickListener {
            navigation.navigateTo(
                "gig/addNewLoginSummary", bundleOf(
                    LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_ADD
                )
            )
        }

        initializeRecyclerView()

    }

    private fun clearExistingListAndRefreshData() {
        currentPage = 1

        tlListing.clear()
        tlLoginSummaryAdapter.submitList(emptyList())
        tlLoginSummaryAdapter.notifyDataSetChanged()

        viewModel.getListingForTL(currentPage)
    }

    private fun initializeRecyclerView() = viewBinding.apply{
        layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )

        datecityRv.layoutManager = layoutManager
        datecityRv.adapter = tlLoginSummaryAdapter
    }

    private fun initSharedViewModel() {
        loginSummarySharedViewModel.loginSummarySharedEvents
            .observe(viewLifecycleOwner,{
                if(!isAdded) return@observe
                clearExistingListAndRefreshData()
            })

    }

    private fun observer() = viewBinding.apply {

        viewModel.loginListing.observe(viewLifecycleOwner, Observer {
            val res = it ?: return@Observer
            when (res) {
                Lce.Loading -> {
                    if(tlLoginSummaryAdapter.itemCount == 0){
                        progressBar.visibility = View.VISIBLE
                    }
                }

                is Lce.Content -> {
                    progressBar.visibility = View.GONE
                    progressBarBottom.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    setupReyclerView(res.content)
                }

                is Lce.Error -> {
                    showToast(getString(R.string.error_loading_data_giger_gigs))
                    progressBar.visibility = View.GONE
                }
            }
        })
    }

    private fun setupReyclerView(res: List<ListingTLModel>)  = viewBinding.apply{
        tlListing.addAll(res)
        if (tlListing.isEmpty()) {
            noData.visibility = View.VISIBLE
            datecityRv.visibility = View.GONE
        } else {
            noData.visibility = View.GONE
            datecityRv.visibility = View.VISIBLE
        }

        if (currentPage == 1){
            Log.d("pag", "zero $currentPage, list : ${res.size}")
            tlLoginSummaryAdapter.submitList(tlListing)
            tlLoginSummaryAdapter.notifyDataSetChanged()
        }else {
            Log.d("pag", "nonzero $currentPage, list : ${res.size}" )
            //tlLoginSummaryAdapter.updateList(res)
            val itemCount = tlLoginSummaryAdapter.itemCount
            tlLoginSummaryAdapter.submitList(tlListing)
            //tlLoginSummaryAdapter.notifyDataSetChanged()
            tlLoginSummaryAdapter.notifyItemChanged(itemCount + 1)
            if (layoutManager.findLastVisibleItemPosition() >= 6 && layoutManager.findFirstVisibleItemPosition()<=6){
                scrollingAdded = false
            }

            //datecityRv.smoothScrollToPosition(tlLoginSummaryAdapter.itemCount/2)

        }

        //val totalPages = tlListing.get(0).totalPages

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
                val lastVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()


                //Log.d("Scrolled", " isLoading: ${isLoading} , currentItemsLatest : $currentItemsLatest, lastVisibleItemPosition: $lastVisibleItemPosition, totalItemsLatest: $totalItemsLatest ")
                //if (isLoading && (currentItemsLatest + lastVisibleItemPosition == totalItemsLatest) && (totalItemsLatest <= tlLoginSummaryAdapter.itemCount)   ) {
                //if ((currentPage < totalPages) && isLoading ){
                if (isLoading && (currentItemsLatest + lastVisibleItemPosition == totalItemsLatest) && (totalItemsLatest <= tlListing.size) ){
                    //load next page
                    currentPage += 1
                    isLoading = false
                    scrollingAdded = true
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

    override fun onBackPressed(): Boolean {
        if (cameFromDeeplink){
            navigation.popBackStack()
            navigation.navigateTo("common/landingScreen")
            return true
        }
        return false
    }
}