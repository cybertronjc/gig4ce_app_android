package com.gigforce.profile.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.profile.onboarding.fragments.agegroup.AgeGroupFragment
import com.gigforce.profile.onboarding.fragments.assetsowned.AssetOwnedFragment
import com.gigforce.profile.onboarding.fragments.experience.ExperienceFragment
import com.gigforce.profile.onboarding.fragments.highestqulalification.HighestQualificationFragment
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import com.gigforce.profile.onboarding.fragments.jobpreference.JobPreferenceFragment
import com.gigforce.profile.onboarding.fragments.namegender.NameGenderFragment
import com.gigforce.profile.onboarding.fragments.preferredJobLocation.OnboardingPreferredJobLocationFragment
import com.gigforce.profile.onboarding.fragments.profilePicture.OnboardingAddProfilePictureFragment

class MutlifragmentAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 9
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NameGenderFragment.newInstance()
            1 -> AgeGroupFragment.newInstance()
            2 -> HighestQualificationFragment.newInstance()
            3 -> ExperienceFragment.newInstance()
            4 -> InterestFragment.newInstance()
            5 -> JobPreferenceFragment.newInstance()
            6 -> AssetOwnedFragment.newInstance()
            7 -> OnboardingAddProfilePictureFragment.newInstance()
            8 -> OnboardingPreferredJobLocationFragment.newInstance()
            else -> {
                throw IllegalStateException("fragment not defined")
            }
        }
    }

}