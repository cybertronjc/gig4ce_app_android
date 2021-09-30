package com.gigforce.giger_gigs.gighistory

//import androidx.navigation.fragment.findNavController
//import com.gigforce.app.R
//import com.gigforce.app.core.base.BaseFragment
//import com.gigforce.user_preferences.PreferencesFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.listeners.PaginationScrollListener
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.giger_gigs.GigPage2Fragment
import com.gigforce.giger_gigs.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gig_history.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [GigHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class GigHistoryFragment : Fragment(), AdapterGigHistory.AdapterGigHistoryCallbacks,
    NoGigsDialog.NoGigsDialogCallbacks {

    @Inject
    lateinit var navigation: INavigation
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(
            GigHistoryViewModel(
                GigHistoryRepository()
            )
        )
    }
    private val viewModel: GigHistoryViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(GigHistoryViewModel::class.java)
    }
    private val adapter by lazy {
        activity?.let { AdapterGigHistory(it) }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gig_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
            )
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        initViews()
        setupRecycler()
        initClicks()
        initObservers()
        viewModel.getData()
        viewModel.observeDocChanges()

    }

    private fun initViews() {
        if (title.isNotBlank())
            tv_title_toolbar_gig_history.text = title
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }


    private fun initClicks() {

        cv_scroll_to_top_gig_hist.setOnClickListener {
            rv_gigs_gig_history.smoothScrollToPosition(0)
        }
        iv_chat_gig_his.setOnClickListener {
            navigation.navigateTo("chats/chatList")
//            navigate(R.id.chatListFragment)
        }
        iv_back_tb_gig_history.setOnClickListener {
//            popBackState()
            navigation.popBackStack()
        }

        cv_profile_image_gig_his.setOnClickListener {
//            navigate(R.id.profileFragment)
            navigation.navigateTo("profile")
        }
//        appBar.setBackButtonListener(View.OnClickListener {
//            navigation.popBackStack()
//        })

//        appBar.apply {
//            setBackButtonListener(View.OnClickListener {
//                navigation.popBackStack()
//            })
//        }


    }

    private fun setupRecycler() {
        rv_gigs_gig_history.adapter = adapter

        adapter?.setCallbacks(this)
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
            adapter?.addOnGoingGigs(
                it?.data,
                viewModel.observableScheduledGigs.value != null && viewModel.observableScheduledGigs.value!!.data != null && viewModel.observableScheduledGigs.value?.data?.isNotEmpty()!!
            )
        })
        viewModel.observableScheduledGigs.observe(viewLifecycleOwner, Observer {
            viewModel.showProgress(false)
            adapter?.addScheduledGigs(it?.data)
            viewModel.isLoading = false
        })
        viewModel.observableShowExplore.observe(viewLifecycleOwner, Observer {
//            val dialog = NoGigsDialog();
//            dialog.setCallbacks(this)
//            dialog.show(parentFragmentManager, NoGigsDialog::class.java.name)
        })
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        viewModel.observableDocChange.observe(viewLifecycleOwner, Observer {
            adapter?.handleDocChange(it)
        })

        var viewModelProfile = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile?.profileAvatarName!!)

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GigHistoryFragment()
    }

    override fun showNoGigExists(int: Int) {
        no_gigs_layout.visibility = int

        if (adapter?.getOngoingGigsCount() != 0) {

        } else {

        }
    }

    override fun getPastGigs() {
        adapter?.clearData()
        viewModel.getGigs(pastGigs = true, resetPageCount = true)
    }

    override fun getUpcomingGigs() {
        adapter?.clearData()
        viewModel.getGigs(pastGigs = false, resetPageCount = true)

    }

    override fun openGigDetails(gig: Gig) {
//        GigNavigation.openGigMainPage(findNavController(), gig.openNewGig(), gig.gigId)
        navigation.navigateTo(
            "gig/attendance", bundleOf(
                GigPage2Fragment.INTENT_EXTRA_GIG_ID to gig.gigId
            )
        )
    }


    override fun getEventState(): Int {
        return viewModel.eventState
    }

    override fun setEventState(state: Int) {
        viewModel.eventState = state
    }

    override fun navigateToExploreByRole() {
//        navigate(R.id.explore_by_role)
        navigation.navigateTo("explorebyrole")
    }

    //TODO : Correct This Code After wards
    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(iv_profile_image_gig_his)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(iv_profile_image_gig_his)
        }
    }


}