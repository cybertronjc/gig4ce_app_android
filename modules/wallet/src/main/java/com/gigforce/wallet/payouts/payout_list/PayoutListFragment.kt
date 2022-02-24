package com.gigforce.wallet.payouts.payout_list

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.core.base.BaseFragment2
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.PayoutListFragmentBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PayoutListFragment : BaseFragment2<PayoutListFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.payout_list_fragment,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "PayoutListFragment"
    }

    private val viewModel : PayoutListViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: PayoutListFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initViewModel()
        }
    }

    private fun initViewModel() {

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    when (it) {
                        is PayoutListViewContract.State.ErrorInLoadingOrUpdatingPayoutList -> errorInLoadingOrUpdatingPayouts(it.error)
                        is PayoutListViewContract.State.LoadingPayoutList -> showPayoutLoading()
                        is PayoutListViewContract.State.ShowOrUpdatePayoutListOnView -> showPayoutListOnView(it.payouts)
                    }
                }
        }

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewEffects
                .collect {

                    when(it){
                        is PayoutListViewContract.UiEffect.OpenPayoutDetailScreen -> openPayoutDetailsScreen(it.payoutId)
                    }
                }
        }
    }

    private fun openPayoutDetailsScreen(
        payoutId: String
    ) {
    }

    private fun errorInLoadingOrUpdatingPayouts(
        error: String
    ) {
    }

    private fun showPayoutListOnView(
        payouts: List<PayoutListPresentationItemData>
    ) {
    }

    private fun showPayoutLoading() {

    }
}