package com.gigforce.profile.onboarding.fragments.agegroup

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import kotlinx.android.synthetic.main.age_group_item.*

class AgeGroupFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(),OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = AgeGroupFragment(formCompletionListener)
    }

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
        return false
    }

    override fun activeNextButton() {
        if(age_group.checkedRadioButtonId!=-1)
            formCompletionListener.enableDisableNextButton(true)
        else formCompletionListener.enableDisableNextButton(false)
    }


}