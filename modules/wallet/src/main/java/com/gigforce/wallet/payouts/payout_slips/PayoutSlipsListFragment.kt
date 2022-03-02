package com.gigforce.wallet.payouts.payout_slips

import android.os.Bundle
import androidx.core.view.isVisible
import com.gigforce.core.base.BaseFragment2
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.PaySlipListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayoutSlipsListFragment : BaseFragment2<PaySlipListFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.pay_slip_list_fragment,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "PayoutListFragment"
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: PaySlipListFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        viewBinding.informationLayout.infoMessageTv.text = "No Payslips to show"
        viewBinding.informationLayout.retryBtn.isVisible = false
    }
}