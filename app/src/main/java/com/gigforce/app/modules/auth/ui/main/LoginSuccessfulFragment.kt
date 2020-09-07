package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.profile.models.ProfileData

class LoginSuccessfulFragment : BaseFragment() {

    companion object {
        fun newInstance() = LoginSuccessfulFragment()
    }

    private val SPLASH_TIME_OUT: Long = 2000 // 1 sec
    var layout: View? = null;
    private lateinit var viewModel: LoginSuccessfulViewModel
    private lateinit var profileData: ProfileData
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        this.setDarkStatusBarTheme()
        layout = inflateView(R.layout.fragment_login_success, inflater, container);
        return layout
    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginSuccessfulViewModel::class.java)
        observer()
//        layout?.setOnClickListener() {
//        }
//        successful_screen.setOnClickListener(){
//            popFragmentFromStack(R.id.homeScreenIcons)
//            navigateWithAllPopupStack(R.id.homeScreenIcons1);
//        }
        Handler().postDelayed({
            viewModel.getProfileData()
        }, SPLASH_TIME_OUT)
    }

    private fun observer() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            profileData = profile
            if (profile != null) {
                if (profile.status) {
                    popFragmentFromStack(R.id.loginSuccessfulFragment)
                    if (profile.isonboardingdone != null && profile.isonboardingdone) {
                        saveOnBoardingCompleted()
                        navigateWithAllPopupStack(R.id.landinghomefragment)
                    } else {
                        popAllBackStates()
                        navigate(
                            R.id.onboardingfragment

                            )
                    }
                } else
                    showToast(profile.errormsg)
            }
        })
    }
}