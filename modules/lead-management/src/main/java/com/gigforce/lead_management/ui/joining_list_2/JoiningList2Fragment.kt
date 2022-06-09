package com.gigforce.lead_management.ui.joining_list_2

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.navigation.LeadManagementConstants
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
//import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentJoiningList2Binding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.changing_tl.ChangeTeamLeaderBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import java.util.HashMap
import javax.inject.Inject

enum class JoiningDataState {
    DEFAULT,
    HAS_DATA,
    NO_DATA
}

@AndroidEntryPoint
class JoiningList2Fragment : BaseFragment2<FragmentJoiningList2Binding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_joining_list_2,
    statusBarColor = R.color.lipstick_2
), IOnBackPressedOverride {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private val viewModel: JoiningList2ViewModel by viewModels()
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    var selectedTab = 0
    var filterDaysFM = -1
    var joiningDataState = JoiningDataState.DEFAULT
    val dropSelectionIds = arrayListOf<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData>()
    var dropJoining : HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>? = HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>()
    var cameFromDeeplink = false

    override fun viewCreated(
        viewBinding: FragmentJoiningList2Binding,
        savedInstanceState: Bundle?
    ) {
        getIntentData(savedInstanceState)
        checkForApplyFilter()
        checkForDropSelection()
        initAppBar()
        initTabLayout()
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()

    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
            cameFromDeeplink = it.getBoolean(StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value) ?: return@let
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
                cameFromDeeplink = it.getBoolean(StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value) ?: return@let
                if (cameFromDeeplink) sharedPreAndCommonUtilInterface.saveDataBoolean("deeplink_onboarding", false)
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value, cameFromDeeplink)

    }

    private fun initListeners(
        viewBinding: FragmentJoiningList2Binding
    ) = viewBinding.apply {

        this.joiningsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.changeTeamLeaderButton.setOnClickListener {
            if(dropSelectionIds.isEmpty()) return@setOnClickListener
            openChangeTeamLeaderScreen(
                ArrayList(
                    dropSelectionIds.map {
                        ChangeTeamLeaderRequestItem(
                            gigerUid = it.gigerId,
                            gigerName = it.gigerName,
                            teamLeaderId = null,
                            joiningId = it._id,
                            gigId = null,
                            jobProfileId = null
                        )
                    }
                )
            )
        }

        this.joinNowButton.setOnClickListener {
            if (joinNowButton.text == getString(R.string.add_new_lead)) {
                logger.d(
                    logTag,
                    "navigating to ${LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING}"
                )

                navigation.navigateTo(
                    dest = LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1,
                    navOptions = getNavOptions()
                )
            }else {

                dropSelectionIds.clear()
                for (entry in dropJoining?.keys!!){
                    dropSelectionIds.add(entry)
                }
                if(dropSelectionIds.isEmpty()){
                    showToast("Select at-least one joining to change team leader")
                }else {
                    openChangeTeamLeaderScreen(
                        ArrayList(
                            dropSelectionIds.map {
                                ChangeTeamLeaderRequestItem(
                                    gigerUid = it.gigerId,
                                    gigerName = it.gigerName,
                                    teamLeaderId = null,
                                    joiningId = it._id,
                                    gigId = null,
                                    jobProfileId = null
                                )
                            }
                        )
                    )
                }


//                dropSelectionIds.clear()
//                for (entry in dropJoining?.keys!!){
//                    dropSelectionIds.add(entry)
//                }
//                if(dropSelectionIds.isEmpty()){
//                    showToast("Select at-least one joining to drop")
//                }else {
//                    DropSelectionBottomSheetDialogFragment.launch(
//                        ArrayList(
//                            dropSelectionIds.map {
//                             it._id
//                        })
//                        , childFragmentManager
//                    )
//                }
            }
        }

        joiningsRecyclerView.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {
                if (dataModel is JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData) {
                    if (dataModel.dropEnabled){
                        val businessName = dataModel.status.split("(").get(0)
                        viewModel.clickDropdown(businessName, false)
                    }else {
                        val businessName = dataModel.status.split("(").get(0)
                        viewModel.clickDropdown(businessName, true)
                    }

                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.resetViewModel()
            viewModel.clearCachedRawJoinings()
            viewModel.getJoinings()
            viewBinding.joinNowButton.text = getString(R.string.add_new_lead)
            if (title.isNotBlank())
                viewBinding.appBarComp.setAppBarTitle(title)
            else
                viewBinding.appBarComp.setAppBarTitle(context?.getString(R.string.joinings_lead))
            viewBinding.appBarComp.setBackButtonDrawable(R.drawable.ic_chevron)
        }
    }

    private fun checkForDropSelection() {
        childFragmentManager.setFragmentResultListener("drop_status", viewLifecycleOwner) { key, bundle ->
            val result = bundle.getString("drop_status")
            // Do something with the result
            if (result == "dropped"){
                viewModel.resetViewModel()
                //viewBinding.appBarComp.setAppBarTitle(getString(R.string.joinings_lead))
                if (title.isNotBlank())
                    viewBinding.appBarComp.setAppBarTitle(title)
                else
                    viewBinding.appBarComp.setAppBarTitle(context?.getString(R.string.joinings_lead))
                viewBinding.joinNowButton.text = getString(R.string.add_new_lead)
                dropJoining?.clear()
                viewModel.getJoinings()
            }
        }
    }

    private fun openChangeTeamLeaderScreen(
        gigers : ArrayList<ChangeTeamLeaderRequestItem>
    ) {
        ChangeTeamLeaderBottomSheetFragment.launch(
            gigers,
            childFragmentManager
        )
    }

    private fun checkForApplyFilter() {
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("filterDays")?.observe(
            viewLifecycleOwner) { result ->
            filterDaysFM = result
            if (filterDaysFM != -1){
                viewBinding.appBarComp.filterDotImageButton.visible()
            } else {
                viewBinding.appBarComp.filterDotImageButton.gone()
            }
            viewModel.filterDaysJoinings(filterDaysFM)
        }

    }

    private fun initAppBar() = viewBinding.appBarComp.apply {
        if (title.isNotBlank())
            setAppBarTitle(title)
        else
            setAppBarTitle(context.getString(R.string.joinings_lead))
        changeBackButtonDrawable()
        makeBackgroundMoreRound()
        makeTitleBold()
        makeHelpVisible(true)
        listeners()
        setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })
        lifecycleScope.launch {
            search_item.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->
                    viewModel.searchJoinings(searchString)
                }
        }

        filterImageButton.setOnClickListener {
            navigation.navigateTo("LeadMgmt/joiningFilter", bundleOf(
                StringConstants.INTENT_FILTER_DAYS_NUMBER.value to filterDaysFM
            ))
        }
    }

    private fun listeners() {
        viewBinding.appBarComp.helpImageButton.setOnClickListener{
            navigation.navigateTo("HelpSectionFragment")
        }
    }

    private fun initTabLayout() = viewBinding.apply {


        statusTabLayout.addTab(statusTabLayout.newTab().setText("Pending (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Completed (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Dropped (0)"))

        val betweenSpace = 25

        val slidingTabStrip: ViewGroup = statusTabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        try {
            //showToast("position: ${selectedTab}")
            statusTabLayout.getTabAt(selectedTab)?.select()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        statusTabLayout.onTabSelected {

            val statusText = it?.text?.split("(")?.get(0).toString().trim()
            viewModel.filterJoinings(statusText)
//            if (statusText == "Dropped") {
//                viewModel.filterJoinings("")
//            } else {
//                viewModel.filterJoinings(statusText)
//            }

            selectedTab = it?.position!!
        }

    }

    private fun initViewModel() {
        viewModel.viewState
            .observe(viewLifecycleOwner, {
                val state = it ?: return@observe

                when (state) {
                    is JoiningList2ViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingJoinings(
                        state.error
                    )
                    is JoiningList2ViewState.JoiningListLoaded -> showJoinings(state.joiningList)
                    JoiningList2ViewState.LoadingDataFromServer -> loadingJoiningsFromServer()
                    JoiningList2ViewState.NoJoiningFound -> showNoJoiningsFound()
                }
            })


        viewModel.filterMap.observe(viewLifecycleOwner, Observer {
            setStatus(it)
        })

        viewModel.getJoinings()

        viewModel.dropJoiningMap.observe(viewLifecycleOwner, Observer {
            setDropSelection(it)
        })
    }

    private fun initSharedViewModel() {
        sharedViewModel
            .viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    LeadManagementSharedViewModelState.JoiningAdded -> {
                        val r = Runnable {
                            try {
                                viewBinding.swipeRefresh.isRefreshing = true
                                viewModel.resetViewModel()
                                //viewBinding.appBarComp.setAppBarTitle(getString(R.string.joinings_lead))
                                if (title.isNotBlank())
                                    viewBinding.appBarComp.setAppBarTitle(title)
                                else
                                    viewBinding.appBarComp.setAppBarTitle(context?.getString(R.string.joinings_lead))
                                viewBinding.joinNowButton.text = getString(R.string.add_new_lead)
                                dropJoining?.clear()
                                viewModel.getJoinings()
                            }catch(e: Exception){
                                e.printStackTrace()
                            }
                        }
                        Handler().postDelayed(r, 1000)

                    }

                    LeadManagementSharedViewModelState.ChangedTeamLeader -> {
                        viewModel.resetViewModel()
                        viewModel.clearCachedRawJoinings()
                        viewModel.getJoinings()
                        viewBinding.joinNowButton.text = getString(R.string.add_new_lead)
                        if (title.isNotBlank())
                            viewBinding.appBarComp.setAppBarTitle(title)
                        else
                            viewBinding.appBarComp.setAppBarTitle(context?.getString(R.string.joinings_lead))
                        viewBinding.appBarComp.setBackButtonDrawable(R.drawable.ic_chevron)
                    }
                }
            })
    }

    private fun setDropSelection(hashMap: HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>?) = viewBinding.apply{
            val count = hashMap?.size
            appBarComp.setAppBarTitle("$count Selected")
            appBarComp.setBackButtonDrawable(R.drawable.ic_baseline_close_24)
//            changeTeamLeaderButton.isVisible = count != 0

            joinNowButton.text = "Change Teamleader"
            dropJoining = hashMap
    }


    private fun setStatus(map: Map<String, Int>) = viewBinding.apply {
        map.forEach {

            if (it.key == LeadManagementConstants.STATUS_PENDING) {
                this.statusTabLayout.getTabAt(0)?.text = "Pending (${it.value})"
            }

            if (it.key == LeadManagementConstants.STATUS_COMPLETED) {
                this.statusTabLayout.getTabAt(1)?.text = "Completed (${it.value})"
            }

            if (it.key == "Dropped") {
                this.statusTabLayout.getTabAt(2)?.text = "Dropped (${it.value})"
//                if (joiningDataState == JoiningDataState.DEFAULT && it.value > 0){
//                    joiningDataState = JoiningDataState.HAS_DATA
//                }else if (joiningDataState == JoiningDataState.DEFAULT && it.value == 0){
//                    joiningDataState = JoiningDataState.NO_DATA
//                }
            }
            checkForNoData()
        }
    }

    private fun loadingJoiningsFromServer() = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        joiningListInfoLayout.root.gone()
        joiningShimmerContainer.visible()

        startShimmer(
            this.joiningShimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }


    private fun showJoinings(
        joiningList: List<JoiningList2RecyclerItemData>
    ) = viewBinding.apply {
        stopShimmer(
            joiningShimmerContainer,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.gone()
        joiningsRecyclerView.collection = joiningList
        swipeRefresh.isRefreshing = false
    }

//    private fun setStatusCount(filters: JoiningFilters) = viewBinding.apply{
//        this.statusTabLayout.removeAllTabs()
//        filters.attendanceStatuses
//        filters.attendanceStatuses?.forEach {
//
//            val newTab = this.statusTabLayout.newTab().apply {
//                this.text = "${it.status} (${it.attendanceCount})"
//                this.tag = it.status
//            }
//            this.statusTabLayout.addTab(newTab)
//        }
//
//        initTabLayout()
//
//    }

    private fun showNoJoiningsFound() = viewBinding.apply {
        joiningsRecyclerView.collection = emptyList()
        stopShimmer(
            joiningShimmerContainer,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()
        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        joiningListInfoLayout.infoIv.layoutParams.height = 800
        joiningListInfoLayout.infoIv.layoutParams.width = 800
        joiningListInfoLayout.infoIv.requestLayout()
        joiningListInfoLayout.infoMessageTv.text = getString(R.string.no_selection_yet_lead)
        swipeRefresh.isRefreshing = false
    }

    private fun showErrorInLoadingJoinings(
        error: String
    ) = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(
            joiningShimmerContainer,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()
        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        joiningListInfoLayout.infoIv.layoutParams.height = 800
        joiningListInfoLayout.infoIv.layoutParams.width = 800
        joiningListInfoLayout.infoIv.requestLayout()
        joiningListInfoLayout.infoMessageTv.text = error
        swipeRefresh.isRefreshing = false
    }

    private fun checkForNoData() = viewBinding.apply {
        if (joiningDataState == JoiningDataState.HAS_DATA){
            statusTabLayout.visible()
            appBarComp.searchImageButton.visible()
            appBarComp.filterFrameLayout.visible()
        }else if (joiningDataState == JoiningDataState.NO_DATA){
            statusTabLayout.gone()
            appBarComp.searchImageButton.gone()
            appBarComp.filterFrameLayout.gone()
        }
    }

    override fun onBackPressed(): Boolean {
        if (cameFromDeeplink){
            navigation.popBackStack()
            navigation.navigateTo("common/calendarScreen")
            return true
        } else if (viewModel.getSelectEnableGlobal()){
            viewModel.resetViewModel()
            viewModel.clearCachedRawJoinings()
            viewModel.getJoinings()
            viewBinding.joinNowButton.text = getString(R.string.add_new_lead)
            if (title.isNotBlank())
                viewBinding.appBarComp.setAppBarTitle(title)
            else
                viewBinding.appBarComp.setAppBarTitle(context?.getString(R.string.joinings_lead))
            viewBinding.appBarComp.setBackButtonDrawable(R.drawable.ic_chevron)
            return true
        }
        return false
    }
}

