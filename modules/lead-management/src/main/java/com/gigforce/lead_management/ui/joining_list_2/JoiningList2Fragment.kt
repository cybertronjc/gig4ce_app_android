package com.gigforce.lead_management.ui.joining_list_2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentJoiningListBinding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData
import com.gigforce.lead_management.ui.giger_onboarding.GigerOnboardingFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class JoiningList2Fragment : BaseFragment2<FragmentJoiningListBinding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_joining_list,
    statusBarColor = R.color.lipstick_2
) {

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: JoiningList2ViewModel by viewModels()


    override fun viewCreated(
        viewBinding: FragmentJoiningListBinding,
        savedInstanceState: Bundle?
    ) {

        initToolbar(viewBinding)
        initTabLayout()
        initListeners(viewBinding)
        initViewModel()
    }


    private fun initListeners(
        viewBinding: FragmentJoiningListBinding
    ) = viewBinding.apply {

        this.joiningsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        PushDownAnim.setPushDownAnimTo(this.joinNowButton).setOnClickListener {
            logger.d(logTag, "navigating to ${LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING}")

            navigation.navigateTo(
                dest = LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1,
                navOptions = getNavOptions()
            )
        }

        viewModel.getJoinings()

    }

    private fun initToolbar(
        viewBinding: FragmentJoiningListBinding
    ) = viewBinding.toolbar.apply {
        this.hideActionMenu()
        this.showTitle(context.getString(R.string.joinings_lead))
        this.setBackButtonListener {
            activity?.onBackPressed()
        }
        this.changeBackButtonDrawable()
        this.showSearchOption(context.getString(R.string.search_joinings_lead))
        lifecycleScope.launchWhenCreated {
            getSearchTextChangeAsFlow()
                .collect { viewModel.searchJoinings(it) }
        }
    }

    private fun initTabLayout() = viewBinding.apply {
        statusTabLayout.addTab(statusTabLayout.newTab().setText("All (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Pending (0)"))
        statusTabLayout.addTab(statusTabLayout.newTab().setText("Completed (0)"))

        val betweenSpace = 50

        val slidingTabStrip: ViewGroup = statusTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until slidingTabStrip.getChildCount() - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        statusTabLayout.onTabSelected {

            val statusText = it?.text?.split("(")?.get(0).toString().trim()
            if (statusText == "All"){
                viewModel.filterJoinings("")
            }else{
                viewModel.filterJoinings(statusText)
            }

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

//        viewModel.filters.observe(viewLifecycleOwner, Observer {
//            setStatusCount(it)
//        })

        viewModel.filterMap.observe(viewLifecycleOwner, Observer {
            setStatus(it)
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
    }

    private fun setStatusCount(filters: JoiningFilters) = viewBinding.apply{
        this.statusTabLayout.removeAllTabs()
        filters.attendanceStatuses
        filters.attendanceStatuses?.forEach {

            val newTab = this.statusTabLayout.newTab().apply {
                this.text = "${it.status} (${it.attendanceCount})"
                this.tag = it.status
            }
            this.statusTabLayout.addTab(newTab)
        }

        initTabLayout()

    }

    private fun showNoJoiningsFound() = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(
            joiningShimmerContainer,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()

        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        joiningListInfoLayout.infoMessageTv.text = getString(R.string.no_job_profiles_found_lead)
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

        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        joiningListInfoLayout.infoMessageTv.text = error
    }
}