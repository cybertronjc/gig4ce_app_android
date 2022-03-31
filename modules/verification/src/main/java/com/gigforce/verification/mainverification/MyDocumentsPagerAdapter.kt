package com.gigforce.verification.mainverification

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.verification.mainverification.compliance.ComplianceDocsFragment

data class TabInfo(
    val fragmentTabName: String
)

class MyDocumentsPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(
    fragment
) {

    companion object {
        val TABS = listOf(
            TabInfo(
                fragmentTabName = "    KYC "
            ),
            TabInfo(
                fragmentTabName = "    Compliance "
            )
        )
    }

    private fun getFragmentAt(index : Int) : Fragment{
        return when (index) {
            0 -> VerificationMainFragment()
            1 -> ComplianceDocsFragment()
            else -> throw IllegalArgumentException("no fragment present for index $index")
        }
    }

    override fun getItemCount(): Int = TABS.size

    override fun createFragment(position: Int): Fragment {
        return getFragmentAt(position)
    }
}