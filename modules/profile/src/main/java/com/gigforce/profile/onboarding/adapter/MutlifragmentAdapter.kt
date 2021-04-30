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

class MutlifragmentAdapter(activity: FragmentActivity,formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 9
    }

    val fragmentArr = ArrayList<Fragment>()
    init {
        fragmentArr.add(NameGenderFragment.newInstance(formCompletionListener))
        fragmentArr.add(AgeGroupFragment.newInstance(formCompletionListener))
        fragmentArr.add(HighestQualificationFragment.newInstance(formCompletionListener))
        fragmentArr.add(OnboardingPreferredJobLocationFragment.newInstance(formCompletionListener))
        fragmentArr.add(ExperienceFragment.newInstance(formCompletionListener))
        fragmentArr.add(InterestFragment.newInstance(formCompletionListener))
        fragmentArr.add(JobPreferenceFragment.newInstance(formCompletionListener))
        fragmentArr.add(AssetOwnedFragment.newInstance(formCompletionListener))
        fragmentArr.add(OnboardingAddProfilePictureFragment.newInstance(formCompletionListener))
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