package com.gigforce.lead_management.ui.joining_list_2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentJoiningList2Binding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.giger_onboarding.GigerOnboardingFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import javax.inject.Inject

@AndroidEntryPoint
class JoiningList2Fragment : BaseFragment2<FragmentJoiningList2Binding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_joining_list_2,
    statusBarColor = R.color.lipstick_2
) {

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: JoiningList2ViewModel by viewModels()
    private val sharedViewModel : LeadManagementSharedViewModel by activityViewModels()
    var selectedTab = 0
    var filterDaysFM = -1

    override fun viewCreated(
        viewBinding: FragmentJoiningList2Binding,
        savedInstanceState: Bundle?
    ) {
        checkForApplyFilter()
        initAppBar()
        initTabLayout()
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun initListeners(
        viewBinding: FragmentJoiningList2Binding
    ) = viewBinding.apply {

        this.joiningsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        PushDownAnim.setPushDownAnimTo(this.joinNowButton).setOnClickListener {
            logger.d(logTag, "navigating to ${LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING}")

            navigation.navigateTo(
                dest = LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1,
                navOptions = getNavOptions()
            )
        }

        joiningsRecyclerView.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {
                if (dataModel is JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData) {
                    Log.d("dropFM", "$dataModel")
                    if (dataModel.dropEnabled){
                        //!dataModel.dropEnabled
                        val businessName = dataModel.status.split("(").get(0)
                        viewModel.clickDropdown(businessName, false)
                        //joiningsRecyclerView.coreAdapter.notifyDataSetChanged()
                    }else {
                        //dataModel.dropEnabled
                        val businessName = dataModel.status.split("(").get(0)
                        viewModel.clickDropdown(businessName, true)
                        //joiningsRecyclerView.coreAdapter.notifyDataSetChanged()
                    }

                }
            }
        }
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

        changeBackButtonDrawable()
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

    private fun initTabLayout() = viewBinding.apply {


        statusTabLayout.addTab(statusTabLayout.newTab().setText("All (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Pending (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Completed (0)"))

        val betweenSpace = 25

        val slidingTabStrip: ViewGroup = statusTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until slidingTabStrip.getChildCount() - 1) {
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
            if (statusText == "All"){
                viewModel.filterJoinings("")
            }else{
                viewModel.filterJoinings(statusText)
            }

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
    }

    private fun initSharedViewModel() {
        sharedViewModel
            .viewState
            .observe(viewLifecycleOwner,{

                when (it) {
                    LeadManagementSharedViewModelState.OneOrMoreSelectionsDropped -> viewModel.getJoinings()
                }
            })
    }

    private fun setStatus(map: Map<String, Int>) = viewBinding.apply{
        map.forEach {
            if (it.key == "All"){
                this.statusTabLayout.getTabAt(0)?.text = "All (${it.value})"
            }
            if (it.key == LeadManagementConstants.STATUS_PENDING){
                this.statusTabLayout.getTabAt(1)?.text = "Pending (${it.value})"
            }
            if (it.key == LeadManagementConstants.STATUS_COMPLETED){
                this.statusTabLayout.getTabAt(2)?.text = "Completed (${it.value})"
            }
        }
    }

    private fun loadingJoiningsFromServer() = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        joiningListInfoLayout.root.gone()
        joiningShimmerContainer.visible()

        startShimmer(
            this.joiningShimmerContainer as LinearLayout,
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
        statusTabLayout.visible()
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.gone()

        joiningsRecyclerView.collection = joiningList
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
        statusTabLayout.gone()
        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        joiningListInfoLayout.infoMessageTv.text = "No Selections Yet !"
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
        joiningListInfoLayout.infoMessageTv.text = error
    }


}