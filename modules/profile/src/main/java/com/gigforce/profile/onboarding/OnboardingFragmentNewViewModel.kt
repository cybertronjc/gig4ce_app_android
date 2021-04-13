package com.gigforce.profile.onboarding

import androidx.lifecycle.ViewModel
import com.gigforce.profile.onboarding.adapter.MultiviewsAdapter
import com.gigforce.profile.onboarding.models.*

class OnboardingFragmentNewViewModel : ViewModel() {

    fun getOnboardingData(): ArrayList<Any> {
        var list = ArrayList<Any>()

        list.add(NameGenderDM(MultiviewsAdapter.NameGenderVT))
        list.add(AgeGroupDM(MultiviewsAdapter.AgeGroupDMVT))
        list.add(HighestQualificationDM(MultiviewsAdapter.HighestQualificationVT))
        list.add(ExperienceDM(MultiviewsAdapter.ExperienceVT))
        list.add(DeliveryExecutiveExperienceDM(MultiviewsAdapter.DeliveryExecutiveExperienceVT))
        list.add(CurrentJobDM(MultiviewsAdapter.CurrentlyWorkingVT))
        list.add(WorkingDays(MultiviewsAdapter.WorkingDaysVT))
        list.add(TimingDM(MultiviewsAdapter.TimingVT))
        list.add(InterestDM(MultiviewsAdapter.InterestVT))

        return list
    }
}