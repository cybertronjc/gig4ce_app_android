package com.gigforce.wallet.payouts.payout_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.wallet.PayoutConstants
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.PayoutDetailsFragmentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PayoutDetailsFragment : BaseBottomSheetDialogFragment<PayoutDetailsFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.payout_details_fragment
) {
    companion object {
        const val TAG = "PayoutDetailsFragment"
    }

    private val viewModel: PayoutDetailsViewModel by viewModels()

    private lateinit var payoutId: String
    private var payout: Payout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            payoutId = it.getString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID) ?: return@let
            payout = it.getParcelable(PayoutConstants.INTENT_EXTRA_PAYOUT_DETAILS)
        }

        savedInstanceState?.let {
            payoutId = it.getString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID) ?: return@let
            payout = it.getParcelable(PayoutConstants.INTENT_EXTRA_PAYOUT_DETAILS)
        }

        viewModel.setPayoutReceivedFromPreviousScreen(
            payoutId = payoutId,
            payout = payout
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID, payoutId)
        outState.putParcelable(PayoutConstants.INTENT_EXTRA_PAYOUT_DETAILS, payout)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: PayoutDetailsFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            initViewModel()
        }
    }

    private fun initView() = viewBinding.apply {

    }

    private fun initViewModel() {

        lifecycleScope.launch {

            viewModel.viewState.collect {
                when (it) {
                    is PayoutDetailsContract.State.ErrorInLoadingPayoutDetails -> errorInLoadingPayoutDetails(
                        it.error
                    )
                    is PayoutDetailsContract.State.LoadingPayoutDetails -> showPayoutLoading()
                    is PayoutDetailsContract.State.ShowPayoutDetails -> showPayoutDetailsOnView(it.payout)
                }
            }
        }

        lifecycleScope.launch {

            viewModel.viewEffects.collect {
                when (it) {
                    is PayoutDetailsContract.UiEffect.CallHelpLineNo -> callPhoneNumber(it.phoneNumber)
                }
            }
        }
    }

    private fun showPayoutLoading() = viewBinding.apply {
        mainLayout.root.gone()
        infoLayout.root.gone()

        shimmerContainer.visible()
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
    }

    private fun showPayoutDetailsOnView(
        payout: Payout
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()
        infoLayout.root.gone()
        mainLayout.root.visible()

        showInfoOnView(payout)
    }

    private fun showInfoOnView(payout: Payout) {
        TODO("Not yet implemented")
    }

    private fun errorInLoadingPayoutDetails(
        error: String
    ) = viewBinding.apply {
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        infoLayout.root.visible()
        infoLayout.infoMessageTv.text = error
    }

    private fun callPhoneNumber(
        phoneNumber: String
    ) {

        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}