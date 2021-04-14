package com.gigforce.profile.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.adapter.MultiviewsAdapter
import com.gigforce.profile.onboarding.adapter.MutlifragmentAdapter
import com.gigforce.profile.onboarding.fragments.experience.ExperienceFragment
import com.gigforce.profile.onboarding.fragments.highestqulalification.HighestQualificationFragment
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import com.gigforce.profile.onboarding.fragments.namegender.NameGenderFragment
import kotlinx.android.synthetic.main.age_group_item.*
import kotlinx.android.synthetic.main.age_group_item.view.*
import kotlinx.android.synthetic.main.experience_item.*
import kotlinx.android.synthetic.main.name_gender_item.view.*
import kotlinx.android.synthetic.main.onboarding_fragment_new_fragment.*


class OnboardingFragmentNew : Fragment() {

    companion object {
        fun newInstance() = OnboardingFragmentNew()
    }

    private lateinit var viewModel: OnboardingFragmentNewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboarding_fragment_new_fragment, container, false)
    }
    var adapter : MultiviewsAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
                viewModel = ViewModelProvider(this).get(OnboardingFragmentNewViewModel::class.java)
        disableViewPagerScroll()
        onboarding_pager.offscreenPageLimit = 6
        activity?.let {
            onboarding_pager.adapter = MutlifragmentAdapter(it,
                object : FragmentInteractionListener {
                    override fun onActionResult(result: Boolean) {
                        enableNextButton(true)
                    }
                })

        }
        next.setOnClickListener(View.OnClickListener {
            saveDataToDB(onboarding_pager.currentItem)
            onboarding_pager.setCurrentItem(onboarding_pager.currentItem+1)
            steps.text = "Steps ${onboarding_pager.currentItem+1}/9"

        })


    }

    private fun disableViewPagerScroll() {
        onboarding_pager.isUserInputEnabled = false
    }

    private fun enableNextButton(enable: Boolean) {
        next.isEnabled = enable

        if (enable) {
            next.background = resources.getDrawable(R.drawable.app_gradient_button, null);
        } else {
            next.background = resources.getDrawable(R.drawable.app_gradient_button_disabled, null);
        }


    }

    private fun saveDataToDB(currentItem: Int) {
        when (currentItem) {
            0 -> {
                var enteredName =
                    onboarding_pager.getChildAt(0).username
                        .text.toString()
                var formattedString = getFormattedString(enteredName)
                viewModel.saveUserName(formattedString.trim())
                saveGender()
            }
            1 -> viewModel.saveAgeGroup(getSelectedAgeGroup())
            2->viewModel.saveHighestQualification(getSelectedHighestQualification())
            3-> setWorkingStatus()
            4->setInterest()
//            2 -> viewModel.selectYourGender(getSelectedDataFromRecycler(2))
//            3 -> viewModel.saveHighestQualification(getSelectedDataFromRecycler(3))
//            4 -> viewModel.saveWorkStatus(getSelectedDataFromRecycler(4))
        }
    }

    private fun saveGender() {
        var nameGenderFragment = (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as NameGenderFragment)
        viewModel.selectYourGender(nameGenderFragment.gender)
    }

    private fun setInterest() {
        var interestFragment = (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as InterestFragment)
        viewModel.saveInterest(interestFragment.selectedInterest)
    }

    private fun setWorkingStatus() {
        var experienceFragment = (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as ExperienceFragment)
        var workStatus = experienceFragment.workStatus
        viewModel.saveWorkStatus(workStatus)

        var total_experience_rg = experienceFragment.total_experience_rg
        val selectedId = total_experience_rg.getCheckedRadioButtonId()
        var radioButton = onboarding_pager.findViewById(selectedId) as RadioButton
        viewModel.saveTotalExperience(radioButton.text.toString())
    }


    private fun getSelectedAgeGroup(): String {
        var radioGroup = ((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)).age_group
        val selectedId: Int = radioGroup.getCheckedRadioButtonId()

        // find the radiobutton by returned id

        // find the radiobutton by returned id
        var radioButton = onboarding_pager.findViewById(selectedId) as RadioButton

        return radioButton.text.toString()
    }

    private fun getSelectedHighestQualification():String{
        return (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as HighestQualificationFragment).selectedHighestQualification
    }

    private fun getFormattedString(enteredName: String): String {
        var formattedString = ""
        var arr = enteredName.split(" ")
        for (str in arr) {
            try {
                formattedString += str.substring(
                    0,
                    1
                ).toUpperCase() + str.substring(1).toLowerCase()
            } catch (e: Exception) {

            }
            formattedString += " "
        }
        return formattedString.trim()
    }

    interface FragmentInteractionListener{
        fun onActionResult(result : Boolean)
    }

    //--------------


}