package com.gigforce.profile.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gigforce.profile.onboarding.OnboardingFragmentNew
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

    val fragmentArr = ArrayList<Fragment>()
    init {
        fragmentArr.add(NameGenderFragment.newInstance())
        fragmentArr.add(AgeGroupFragment.newInstance())
        fragmentArr.add(HighestQualificationFragment.newInstance())
        fragmentArr.add(OnboardingPreferredJobLocationFragment.newInstance())
        fragmentArr.add(ExperienceFragment.newInstance())
        fragmentArr.add(InterestFragment.newInstance())
        fragmentArr.add(JobPreferenceFragment.newInstance())
        fragmentArr.add(AssetOwnedFragment.newInstance())
        fragmentArr.add(OnboardingAddProfilePictureFragment.newInstance())
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentArr.get(position)
    }

    override fun getItemId(position: Int): Long {
        (return super.getItemId(position))
    }


    fun getFragment(position: Int):Fragment{
        return fragmentArr.get(position)
    }
}