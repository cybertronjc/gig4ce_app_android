package com.gigforce.giger_gigs.tl_login_details

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.listeners.PaginationScrollListener
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.adapters.TLLoginSummaryAdapter
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.util.*
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
    val TOTAL_PAGES = 10

    private val tlLoginSummaryAdapter: TLLoginSummaryAdapter by lazy {
        TLLoginSummaryAdapter(requireContext(), this).apply {
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
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

    private val INTERVAL_TIME: Long = 1000 * 5
    var hadler = Handler()
    fun refreshListHandler() {
        hadler.postDelayed({
            try {
                if (!onpaused) {
                    initializeViews()
                    refreshListHandler()
                }
            } catch (e: Exception) {

            }

        }, INTERVAL_TIME)

    }

    var onpaused = false
    override fun onPause() {
        super.onPause()
        onpaused = true
        hadler.removeCallbacks(null)
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
        //loadFirstPage()
        viewModel.getListingForTL(0)
    }


    private fun listeners() = viewBinding.apply {
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
                    setupReyclerView(res.content)
                }

                is Lce.Error -> {
                    showToast("Error loading data")
                    progressBar.visibility = View.GONE
                }
            }
        })
    }

    private fun setupReyclerView(res: List<ListingTLModel>) {

        if (res.isEmpty()) {
            viewBinding.noData.visibility = View.VISIBLE
            viewBinding.datecityRv.visibility = View.GONE
        } else {
            viewBinding.noData.visibility = View.GONE
            viewBinding.datecityRv.visibility = View.VISIBLE
        }
        val layoutManager = LinearLayoutManager(context)
        viewBinding.datecityRv.layoutManager = layoutManager
        tlLoginSummaryAdapter.submitList(res)
        viewBinding.datecityRv.adapter = tlLoginSummaryAdapter

//        if (currentPage <= TOTAL_PAGES) {
//            //show loader
//            viewBinding.progressBarBottom.visibility = View.VISIBLE
//        }
//        else{
//            isLastPage = true
//        }
//
//        viewBinding.datecityRv.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
//            override fun isLastPage(): Boolean {
//                return isLastPage
//            }
//
//            override fun isLoading(): Boolean {
//                return isLoading
//            }
//
//            override fun loadMoreItems() {
//                isLoading = true;
//                currentPage += 1;
//
//                loadNextPage();
//            }
//
//        })
    }

//    private fun loadNextPage() {
////        onpaused = true
////        viewBinding.progressBarBottom.visibility = View.GONE
////        viewModel.getListingForTL(currentPage)
//    }
//
//    private fun loadFirstPage(){
//        Log.d("LIST", "loadFirstPage: ");
//        currentPage = PAGE_START;
//
//        viewModel.getListingForTL(currentPage)
//    }

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