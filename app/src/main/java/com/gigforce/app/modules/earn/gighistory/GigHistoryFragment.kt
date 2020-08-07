package com.gigforce.app.modules.earn.gighistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.PaginationScrollListener
import com.gigforce.app.utils.ViewModelProviderFactory
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_gig_history.*


/**
 * A simple [Fragment] subclass.
 * Use the [GigHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GigHistoryFragment : BaseFragment(), AdapterGigHistory.AdapterGigHistoryCallbacks,
    NoGigsDialog.NoGigsDialogCallbacks {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(GigHistoryViewModel(GigHistoryRepository()))
    }
    private val viewModel: GigHistoryViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(GigHistoryViewModel::class.java)
    }
    private val adapter by lazy {
        AdapterGigHistory()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_gig_history, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        initClicks()
        initObservers()
        viewModel.getData()
    }


    private fun initClicks() {

        cv_scroll_to_top_gig_hist.setOnClickListener {
            rv_gigs_gig_history.smoothScrollToPosition(0)
        }
        iv_chat_gig_his.setOnClickListener {
            navigate(R.id.contactScreenFragment)
        }
        iv_back_tb_gig_history.setOnClickListener {
            popBackState()
        }
    }

    private fun setupRecycler() {
        rv_gigs_gig_history.adapter = adapter
        adapter.setCallbacks(this)
        val layoutManager = LinearLayoutManager(activity)
        rv_gigs_gig_history.layoutManager = layoutManager
//        rv_gigs_gig_history.addItemDecoration(
//            GigHistoryItemDecorator(
//                requireContext().resources.getDimensionPixelOffset(
//                    R.dimen.size_16
//                )
//            )
//        )
        rv_gigs_gig_history?.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return viewModel.isLastPage
            }

            override fun isLoading(): Boolean {
                return viewModel.isLoading
            }

            override fun loadMoreItems() {
                viewModel.isLoading = true
                //you have to call loadmore items to get more data
                viewModel.getGigs(
                    viewModel.pastGigs,
                    false

                )
            }
        })
    }

    private fun initObservers() {
        viewModel.observerShowProgress.observe(viewLifecycleOwner, Observer {
            pb_gig_hist.visibility = it!!
        })
        viewModel.observableOnGoingGigs.observe(viewLifecycleOwner, Observer {
            viewModel.showProgress(false)
            adapter.addOnGoingGigs(it?.data)
        })
        viewModel.observableScheduledGigs.observe(viewLifecycleOwner, Observer {
            viewModel.showProgress(false)
            adapter.addScheduledGigs(it?.data)
            viewModel.isLoading = false
        })
        viewModel.observableShowExplore.observe(viewLifecycleOwner, Observer {
            val dialog = NoGigsDialog();
            dialog.setCallbacks(this)
            dialog.show(parentFragmentManager, NoGigsDialog::class.java.name)
        })
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        //TODO : Correct this afterwards
        var viewModelProfile = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GigHistoryFragment()
    }

    override fun showNoGigExists(int: Int) {
        tv_no_gigs_gig_hist.visibility = int
    }

    override fun getPastGigs() {
        adapter.clearData();
        viewModel.getGigs(pastGigs = true, resetPageCount = true)
    }

    override fun getUpcomingGigs() {
        adapter.clearData()
        viewModel.getGigs(pastGigs = false, resetPageCount = true)

    }

    override fun openGigDetails(gig: Gig) {
        navigate(R.id.presentGigPageFragment, Bundle().apply {
            this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, gig.gigId)
        })

    }


    override fun getEventState(): Int {
        return viewModel.eventState
    }

    override fun setEventState(state: Int) {
        viewModel.eventState = state
    }

    override fun navigateToExploreByRole() {
        navigate(R.id.explore_by_role)
    }

    //TODO : Correct This Code After wards
    private fun displayImage(profileImg: String) {
        if (profileImg != null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(iv_profile_image_gig_his)
        }
    }


}