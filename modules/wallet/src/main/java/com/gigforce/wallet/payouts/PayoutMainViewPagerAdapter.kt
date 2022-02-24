package com.gigforce.wallet.payouts

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.wallet.payouts.payout_list.PayoutListFragment

data class TabInfo(
    val fragment: Fragment,
    val fragmentTabName: String
)

class PayoutMainViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(
    fragment
) {

    companion object {
        val TABS = listOf(
            TabInfo(
                fragment = PayoutListFragment(),
                fragmentTabName = "Payouts"
            ),
//            TabInfo(
//                fragment = PayoutSlipsListFragment(),
//                fragmentTabName = "Payout Slips"
//            )
        )
    }

    override fun getItemCount(): Int = TABS.size

    override fun createFragment(position: Int): Fragment {
         return TABS[position].fragment
    }
}