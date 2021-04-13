package com.gigforce.profile.onboarding.adapter

import android.app.Activity
import android.widget.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.profile.onboarding.fragments.agegroup.AgeGroupFragment
import com.gigforce.profile.onboarding.fragments.assetsowned.AssetOwnedFragment
import com.gigforce.profile.onboarding.fragments.experience.ExperienceFragment
import com.gigforce.profile.onboarding.fragments.highestqulalification.HighestQualificationFragment
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import com.gigforce.profile.onboarding.fragments.jobpreference.JobPreferenceFragment
import com.gigforce.profile.onboarding.fragments.namegender.NameGenderFragment

class MutlifragmentAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NameGenderFragment.newInstance()
            1 -> AgeGroupFragment.newInstance()
            2 -> HighestQualificationFragment.newInstance()
            3 -> HighestQualificationFragment.newInstance()
            4 -> ExperienceFragment.newInstance()
            5 -> InterestFragment.newInstance()
            6-> JobPreferenceFragment.newInstance()
            else->AssetOwnedFragment.newInstance()
        }
    }

}