package com.gigforce.app.modules.onboardingmain

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.ui.main.LoginSuccessfulViewModel

class OnboardingLoaderFragment: BaseFragment() {
    companion object {
        fun newInstance() = OnboardingLoaderFragment()
    }
    private lateinit var viewModel: LoginSuccessfulViewModel
    private val SPLASH_TIME_OUT: Long = 250
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.onboarding_loader_fragment, inflater, container)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginSuccessfulViewModel::class.java)
//        val onboardingCompleted = isOnBoardingCompleted()
//        if(onboardingCompleted!=null && onboardingCompleted.equals("true")){
//            navigateToHomeScreen()
//        }
        observer()
        Handler().postDelayed({
            viewModel.getProfileData()
        }, SPLASH_TIME_OUT)
    }
    private fun navigateToLandingHomeScreen() {
        popFragmentFromStack(R.id.onboardingLoaderfragment)
        navigate(R.id.landinghomefragment)
    }
    private fun navigateToMainOnboarding(){
        popFragmentFromStack(R.id.onboardingLoaderfragment)
        navigate(R.id.onboardingfragment)
    }
    private fun observer() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            if (profile != null ) {
                if (profile.status) {
                    if (profile.isonboardingdone) {
                        saveOnBoardingCompleted()
                        navigateToLandingHomeScreen()
                    }else {
                        navigateToMainOnboarding()
                    }
                } else
                    showToast(profile.errormsg)
            }
        })
    }

}