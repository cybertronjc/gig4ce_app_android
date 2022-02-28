package com.gigforce.wallet.payouts

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.wallet.payouts.payout_list.PayoutListFragment
import com.gigforce.wallet.payouts.payout_slips.PayoutSlipsListFragment

data class TabInfo(
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
                fragmentTabName = "Payouts"
            ),
            TabInfo(
                fragmentTabName = "Payout Slips"
            )
        )
    }

    private fun getFragmentAt(index : Int) : Fragment{
       return when (index) {
            0 -> PayoutListFragment()
            1 -> PayoutSlipsListFragment()
            else -> throw IllegalArgumentException("no fragment present for index $index")
        }
    }

    override fun getItemCount(): Int = TABS.size

    override fun createFragment(position: Int): Fragment {
         return getFragmentAt(position)
    }
}