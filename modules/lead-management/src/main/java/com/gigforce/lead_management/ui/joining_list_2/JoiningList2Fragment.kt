package com.gigforce.lead_management.ui.joining_list_2

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentJoiningListBinding
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.ui.giger_onboarding.GigerOnboardingFragment
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
                dest = LeadManagementNavDestinations.FRAGMENT_GIGER_ONBOARDING,
                args = bundleOf(
                    GigerOnboardingFragment.INTENT_CAME_FROM_JOINING to true
                ),
                navOptions = getNavOptions()
            )
        }

    }

    private fun initToolbar(
        viewBinding: FragmentJoiningListBinding
    ) = viewBinding.toolbar.apply {
        this.hideActionMenu()
        this.showTitle(context.getString(R.string.joinings_lead))
        this.setBackButtonListener {
            activity?.onBackPressed()
        }

        this.showSearchOption(context.getString(R.string.search_joinings_lead))
        lifecycleScope.launchWhenCreated {
            getSearchTextChangeAsFlow()
                .collect { viewModel.searchJoinings(it) }
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
        joiningList: List<JoiningListRecyclerItemData>
    ) = viewBinding.apply {
        stopShimmer(
            joiningShimmerContainer,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.gone()

        joiningsRecyclerView.collection = joiningList
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