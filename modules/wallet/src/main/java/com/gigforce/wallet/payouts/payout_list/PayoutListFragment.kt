package com.gigforce.wallet.payouts.payout_list

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.wallet.PayoutNavigation
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.PayoutListFragmentBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class PayoutListFragment : BaseFragment2<PayoutListFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.payout_list_fragment,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "PayoutListFragment"
    }

    @Inject
    lateinit var payoutNavigation: PayoutNavigation
    private val viewModel: PayoutListViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: PayoutListFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    private fun initView() = viewBinding.apply{

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(PayoutDiffUtilCallback())

        swipeRefresh.setOnRefreshListener {
            viewModel.handleEvent(PayoutListViewContract.UiEvent.RefreshPayoutListClicked)
        }
    }

    private fun initViewModel() {

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    when (it) {
                        is PayoutListViewContract.State.ErrorInLoadingOrUpdatingPayoutList -> errorInLoadingOrUpdatingPayouts(
                            it.error
                        )
                        is PayoutListViewContract.State.LoadingPayoutList -> showPayoutLoading()
                        is PayoutListViewContract.State.ShowOrUpdatePayoutListOnView -> showPayoutListOnView(
                            it.payouts
                        )
                    }
                }
        }

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewEffects
                .collect {

                    when (it) {
                        is PayoutListViewContract.UiEffect.OpenPayoutDetailScreen -> openPayoutDetailsScreen(
                            it.payoutId
                        )
                    }
                }
        }
    }

    private fun openPayoutDetailsScreen(
        payoutId: String
    ) = payoutNavigation.openPayoutDetailsScreen(payoutId)

    private fun errorInLoadingOrUpdatingPayouts(
        error: String
    ) = viewBinding.apply {
        swipeRefresh.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        if(recyclerView.childCount == 0){

            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = error
        } else {
            infoLayout.root.gone()

            Snackbar.make(
                rootFrameLayout,
                error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showPayoutListOnView(
        payouts: List<PayoutListPresentationItemData>
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()
        infoLayout.root.gone()
        recyclerView.collection = payouts
        swipeRefresh.isRefreshing = false

        showOrHideNoPayoutsLayout(payouts.size)
    }

    private fun showOrHideNoPayoutsLayout(
        payoutsCount: Int
    ) = viewBinding.apply {

        if (payoutsCount == 0) {
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = "No Payouts to show"
        } else {
            infoLayout.root.gone()
            infoLayout.infoMessageTv.text = null
        }
    }

    private fun showPayoutLoading() = viewBinding.apply {
        infoLayout.root.gone()

        if (recyclerView.childCount == 0) {

            shimmerContainer.visible()
            swipeRefresh.isRefreshing = false

            startShimmer(
                this.shimmerContainer,
                ShimmerDataModel(
                    minHeight = R.dimen.size_120,
                    minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                    marginRight = R.dimen.size_16,
                    marginTop = R.dimen.size_1,
                    orientation = LinearLayout.VERTICAL
                ),
                R.id.shimmer_controller
            )
        } else {

            swipeRefresh.isRefreshing = true
            shimmerContainer.gone()
            stopShimmer(
                this.shimmerContainer,
                R.id.shimmer_controller
            )
        }
    }
}