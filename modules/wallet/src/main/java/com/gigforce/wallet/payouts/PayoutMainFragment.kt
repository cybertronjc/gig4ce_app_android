package com.gigforce.wallet.payouts

import android.os.Bundle
import com.gigforce.core.base.BaseFragment2
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.FragmentPayoutMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class PayoutMainFragment : BaseFragment2<FragmentPayoutMainBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_payout_main,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "PayoutMainFragment"
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentPayoutMainBinding,
        savedInstanceState: Bundle?
    ) {
        viewBinding.viewPager.adapter = PayoutMainViewPagerAdapter(this)

        TabLayoutMediator(
            viewBinding.tablayout,
            viewBinding.viewPager
        ) { tab, position ->
            tab.text = PayoutMainViewPagerAdapter.TABS[position].fragmentTabName
        }.attach()
    }
}