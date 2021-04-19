package com.gigforce.profile.onboarding

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.adapter.MutlifragmentAdapter
import com.gigforce.profile.onboarding.fragments.assetsowned.AssetOwnedFragment
import com.gigforce.profile.onboarding.fragments.experience.ExperienceFragment
import com.gigforce.profile.onboarding.fragments.highestqulalification.HighestQualificationFragment
import com.gigforce.profile.onboarding.fragments.interest.InterestFragment
import com.gigforce.profile.onboarding.fragments.jobpreference.JobPreferenceFragment
import com.gigforce.profile.onboarding.fragments.namegender.NameGenderFragment
import com.gigforce.profile.onboarding.fragments.preferredJobLocation.OnboardingPreferredJobLocationFragment
import com.gigforce.profile.onboarding.fragments.profilePicture.OnboardingAddProfilePictureFragment
import com.gigforce.profile.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.age_group_item.*
import kotlinx.android.synthetic.main.experience_item.*
import kotlinx.android.synthetic.main.name_gender_item.view.*
import kotlinx.android.synthetic.main.onboarding_fragment_new_fragment.*
import kotlinx.android.synthetic.main.onboarding_fragment_new_fragment_greeting_layout.*

//@AndroidEntryPoint
class OnboardingFragmentNew : Fragment() {

    companion object {
        fun newInstance() = OnboardingFragmentNew()
    }

    //    @Inject lateinit var navigation : INavigation
    private lateinit var viewModel: OnboardingFragmentNewViewModel
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private var win: Window? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboarding_fragment_new_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setUpViewForOnboarding()
        //changeStatusBarColor()

        Glide.with(requireContext()).load(R.drawable.gif_hello).into(hi_there_image)
        onboarding_get_started_btn.setOnClickListener {

            onboarding_greeting_layout.gone()
            setUpViewForOnboarding()
            enableNextButton(false)
        }
    }

    private fun setUpViewForOnboarding() {
        next.visible()
        onboarding_root_layout.visible()

        viewModel = ViewModelProvider(this).get(OnboardingFragmentNewViewModel::class.java)
        disableViewPagerScroll()
        onboarding_pager.offscreenPageLimit = 3
        activity?.let {
            onboarding_pager.adapter =
                MutlifragmentAdapter(it, object : OnFragmentFormCompletionListener {
                    override fun enableDisableNextButton(validate: Boolean) {
                        enableNextButton(validate)
                    }

                })

        }
        next.setOnClickListener {
            if (isFragmentActionNotExists()) {
                saveDataToDB(onboarding_pager.currentItem)
                onboarding_pager.currentItem = onboarding_pager.currentItem + 1
                steps.text = "Steps ${onboarding_pager.currentItem + 1}/9"
                if (onboarding_pager.currentItem == 3)
                    enableNextButton(true)
                else
                    enableNextButton(false)
            }

        }

        backpressicon.setOnClickListener(View.OnClickListener {
            if (!isFragmentLastStateFound())
                onboarding_pager.currentItem = onboarding_pager.currentItem - 1
        })


        onboarding_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 8) {
                    val fragmentAdapter = onboarding_pager.adapter as MutlifragmentAdapter
                    val fragment =
                        fragmentAdapter.getFragment(position) as OnboardingAddProfilePictureFragment
                    fragment.showCameraSheetIfNotShown()
                }
            }
        })
    }

    private fun changeStatusBarColor(){
        win = activity?.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_gray))
    }

    private fun isFragmentLastStateFound(): Boolean {
        var currentFragment =
            ((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem))
        if (currentFragment is FragmentSetLastStateListener) {
            return currentFragment.lastStateFormFound()
        }
        return false
    }

    private fun isFragmentActionNotExists(): Boolean {
        var currentFragment =
            ((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem))
        if (currentFragment is FragmentInteractionListener) {
            return !currentFragment.nextButtonActionFound()
        }
        return true
    }

    private fun disableViewPagerScroll() {
        onboarding_pager.isUserInputEnabled = false
    }

    private fun enableNextButton(enable: Boolean) {
        next.isEnabled = enable

        if (enable) {
            next.background = resources.getDrawable(R.drawable.app_gradient_button, null)
        } else {
            next.background = resources.getDrawable(R.drawable.app_gradient_button_disabled, null)
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
                hideKeyboard()
            }
            1 -> viewModel.saveAgeGroup(getSelectedAgeGroup())
            2 -> viewModel.saveHighestQualification(getSelectedHighestQualification())
            3 -> {
                val fragmentAdapter = onboarding_pager.adapter as MutlifragmentAdapter
                val fragment =
                    fragmentAdapter.getFragment(currentItem) as OnboardingPreferredJobLocationFragment
                val selectedCity = fragment.getSelectedCity()

                if (selectedCity != null) {

                    onboardingViewModel.savePreferredJobLocation(
                        cityId = selectedCity.id,
                        cityName = selectedCity.name,
                        stateCode = selectedCity.stateCode,
                        subLocation = selectedCity.subLocation
                    )
                }
            }
            4 -> setWorkingStatus()
            5 -> setInterest()
            6 -> setJobPreference()
            7 -> setAssetsData()
            8 -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModel.onboardingCompleted()
        navigateToLoaderScreen()
    }

    private fun navigateToLoaderScreen() {
//        navigation.popAllBackStates()
//        navigation.navigateTo("loader_screen")
    }

    private fun setAssetsData() {
        var assetsowned =
            (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as AssetOwnedFragment)
        viewModel.saveAssets(assetsowned.getAssetsData())
    }

    private fun setJobPreference() {
        var jobPreferenceFragment =
            (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as JobPreferenceFragment)
        viewModel.saveJobPreference(jobPreferenceFragment.fullTimeJob)
        viewModel.saveDaysPreference(jobPreferenceFragment.getWorkingDays())
        viewModel.saveTimeSlots(jobPreferenceFragment.getTimeSlots())
    }

    private fun saveGender() {
        var nameGenderFragment =
            (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as NameGenderFragment)
        viewModel.selectYourGender(nameGenderFragment.gender)
    }

    private fun setInterest() {
        var interestFragment =
            (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as InterestFragment)
        viewModel.saveInterest(interestFragment.getselectedInterest())
    }

    private fun setWorkingStatus() {
        var experienceFragment =
            (((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)) as ExperienceFragment)
        var workStatus = experienceFragment.workStatus
        viewModel.saveWorkStatus(workStatus)

        var total_experience_rg = experienceFragment.total_experience_rg
        val selectedId = total_experience_rg.checkedRadioButtonId
//        var radioButton = onboarding_pager.findViewById(selectedId) as RadioButton
//        viewModel.saveTotalExperience(radioButton.text.toString())
    }


    private fun getSelectedAgeGroup(): String {
        var radioGroup =
            ((onboarding_pager.adapter as MutlifragmentAdapter).getFragment(onboarding_pager.currentItem)).age_group
        val selectedId: Int = radioGroup.checkedRadioButtonId

        // find the radiobutton by returned id

        // find the radiobutton by returned id
        // var radioButton = onboarding_pager.findViewById(selectedId) as RadioButton

        // return radioButton.text.toString()
        return ""
    }

    private fun getSelectedHighestQualification(): String {
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

    interface FragmentSetLastStateListener {
        fun lastStateFormFound(): Boolean
    }

    interface FragmentInteractionListener {
        fun nextButtonActionFound(): Boolean
    }

    interface OnFragmentFormCompletionListener {
        fun enableDisableNextButton(validate: Boolean)
    }

    fun hideKeyboard() {
        activity?.let {
            val imm: InputMethodManager =
                it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = it.currentFocus ?: null
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    //--------------


}