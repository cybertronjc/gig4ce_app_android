package com.gigforce.profile.onboarding.fragments.agegroup

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.age_group_item.*
import kotlinx.android.synthetic.main.name_gender_item.*
import javax.inject.Inject

@AndroidEntryPoint
class AgeGroupFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(),OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = AgeGroupFragment(formCompletionListener)
    }

    @Inject lateinit var eventTracker: IEventTracker
    private var win: Window? = null
    private lateinit var viewModel: AgeGroupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.age_group_item, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AgeGroupViewModel::class.java)
        age_group.setOnCheckedChangeListener{ group, checkedId ->
            formCompletionListener.enableDisableNextButton(true)
        }
    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        val radioButton = age_group.findViewById(age_group.checkedRadioButtonId) as RadioButton
        var age = radioButton.text.toString()
        var map = mapOf("age_group" to age)
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_AGE_GROUP,map))
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("Age Group", age))

        return false
    }

    override fun activeNextButton() {
        if(age_group.checkedRadioButtonId!=-1)
            formCompletionListener.enableDisableNextButton(true)
        else formCompletionListener.enableDisableNextButton(false)
    }


}