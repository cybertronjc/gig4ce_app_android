package com.gigforce.verification.mainverification

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.verification.mainverification.compliance.ComplianceDocsFragment


private const val NUM_TABS = 2
class MyDocumentsPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return VerificationMainFragment()
            1 -> return ComplianceDocsFragment()
        }
        return  VerificationMainFragment()
    }

}