package com.gigforce.lead_management.ui.joining_list

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementSharedViewState
import com.gigforce.lead_management.R
import com.gigforce.lead_management.SharedLeadManagementViewModel
import com.gigforce.lead_management.databinding.FragmentJoiningListBinding
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class JoiningListFragment : BaseFragment2<FragmentJoiningListBinding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_joining_list,
    statusBarColor = R.color.lipstick_2
) {

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: JoiningListViewModel by viewModels()
    private val sharedLeadViewModel: SharedLeadManagementViewModel by activityViewModels()

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
            logger.d(logTag,"navigating to LeadMgmt/gigerOnboarding")

            navigation.navigateTo(
                dest = "LeadMgmt/referenceCheckFragment"/*"LeadMgmt/gigerOnboarding"*/,
                args = bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_USER_UID to "qxikOJuJSqXvXyOKgv5usgWZmW82"
                ),
                navOptions = navOptions {
                    this.anim {
                        this.enter = R.anim.nav_default_enter_anim
                        this.exit = R.anim.nav_default_exit_anim
                        this.popEnter= R.anim.nav_default_pop_enter_anim
                        this.popExit= R.anim.nav_default_pop_exit_anim
                    }
                }
            )
        }

    }

    private fun initToolbar(
        viewBinding: FragmentJoiningListBinding
    ) = viewBinding.toolbar.apply {
        this.hideActionMenu()
        this.showTitle("Joinings")
        this.setBackButtonListener {
            activity?.onBackPressed()
        }

        this.showSearchOption("Search Joinings")
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
                    is JoiningListViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingJoinings(
                        state.error
                    )
                    is JoiningListViewState.JoiningListLoaded -> showJoinings(state.joiningList)
                    JoiningListViewState.LoadingDataFromServer -> loadingJoiningsFromServer()
                    JoiningListViewState.NoJoiningFound -> showNoJoiningsFound()
                }
            })
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
        joiningList: List<JoiningListRecyclerItemData>
    ) = viewBinding.apply {
        stopShimmer(
            joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.gone()

        joiningsRecyclerView.collection = joiningList
    }

    private fun showNoJoiningsFound() = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(
            joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()

        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        joiningListInfoLayout.infoMessageTv.text = "No Job Profile Found"
    }

    private fun showErrorInLoadingJoinings(
        error: String
    ) = viewBinding.apply {

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(
            joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()

        joiningListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        joiningListInfoLayout.infoMessageTv.text = error
    }
}