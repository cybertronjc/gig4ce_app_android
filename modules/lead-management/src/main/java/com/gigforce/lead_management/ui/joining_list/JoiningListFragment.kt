package com.gigforce.lead_management.ui.joining_list

import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.LeadManagementSharedViewState
import com.gigforce.lead_management.R
import com.gigforce.lead_management.SharedLeadManagementViewModel
import com.gigforce.lead_management.databinding.FragmentJoiningListBinding
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import kotlinx.coroutines.flow.collect

class JoiningListFragment : BaseFragment2<FragmentJoiningListBinding>(
    fragmentName = "JoiningListFragment",
    layoutId = R.layout.fragment_joining_list,
    statusBarColor = R.color.lipstick_2
) {
    private val viewModel: JoiningListViewModel by viewModels()
    private val sharedLeadViewModel: SharedLeadManagementViewModel by activityViewModels()

    override fun viewCreated(viewBinding: FragmentJoiningListBinding) {
        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
    }

    private fun initListeners(
        viewBinding: FragmentJoiningListBinding
    ) = viewBinding.apply {

        this.joinNowButton.setOnClickListener {

            //todo redirec to no check screen
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
        sharedLeadViewModel.joiningsSharedViewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    LeadManagementSharedViewState.LeadsChangedRefreshJoinings -> viewModel.refreshJoinings()
                }
            })

        viewModel.viewState
            .observe(viewLifecycleOwner, {
                val state = it ?: return@observe

                when (state) {
                    is JoiningListViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingJoinings(state.error)
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
    ) = viewBinding.apply{
        stopShimmer(joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.gone()

        joiningsRecyclerView.collection = joiningList
    }

    private fun showNoJoiningsFound() = viewBinding.apply{

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()

        //todo show illus here
    }

    private fun showErrorInLoadingJoinings(
        error: String
    ) = viewBinding.apply{

        joiningsRecyclerView.collection = emptyList()
        stopShimmer(joiningShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        joiningShimmerContainer.gone()
        joiningListInfoLayout.root.visible()
        joiningListInfoLayout
    }
}