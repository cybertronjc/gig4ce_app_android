package com.gigforce.app.modules.auth

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.auth.ui.main.LoginSuccessfulViewModel
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.profile.ProfileData

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingLoaderFragment : BaseFragment() {
    companion object {
        fun newInstance() =
            OnboardingLoaderFragment()
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    private lateinit var viewModel: LoginSuccessfulViewModel

    @Inject lateinit var shareDataAndCommUtil : SharedPreAndCommonUtilInterface

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
        observer()
        Handler().postDelayed({
            viewModel.getProfileAndGigData()
        }, SPLASH_TIME_OUT)
    }

    private fun navigateToLandingHomeScreen() {
        popFragmentFromStack(R.id.onboardingLoaderfragment)
        navigate(R.id.landinghomefragment)
    }

    private fun navigateToMainOnboarding() {
        popFragmentFromStack(R.id.onboardingLoaderfragment)
        navigate(R.id.onboardingfragment)
    }

    private fun observer() {

        viewModel.userProfileAndGigData.observe(viewLifecycleOwner, Observer { profileAndGig ->

            if (profileAndGig.profile != null) {
                setUserInCrashlytics(profileAndGig.profile)
                if (profileAndGig.profile.status) {

                    if (profileAndGig.profile.isonboardingdone) {
                        saveOnBoardingCompleted()
                        shareDataAndCommUtil.saveLoggedInUserName(profileAndGig.profile.name)
                        if (!checkForDeepLink()) {
                            if (profileAndGig.hasGigs) {
                                navigateToCalendarHomeScreen()
                            } else {
                                navigateToLandingHomeScreen()
                            }
                        }
                    } else {
                        navigateToMainOnboarding()
                    }
                } else
                    showToast(profileAndGig.profile.errormsg)
            }
        })
    }

    private fun checkForDeepLink(): Boolean {
        if (navFragmentsData?.getData()
                        ?.getBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, false)!!
        ) {
            popFragmentFromStack(R.id.onboardingLoaderfragment)
            navigate(
                    R.id.fragment_role_details, bundleOf(
                    StringConstants.ROLE_ID.value to navFragmentsData?.getData()
                            ?.getString(StringConstants.ROLE_ID.value),
                    StringConstants.ROLE_VIA_DEEPLINK.value to true,
                    StringConstants.INVITE_USER_ID.value to navFragmentsData?.getData()
                            ?.getString(StringConstants.INVITE_USER_ID.value)
            )

            )
            navFragmentsData?.getData()?.putBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, false)
            return true

        } else if (navFragmentsData?.getData()
                        ?.getBoolean(StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value, false)!!
        ) {

            popFragmentFromStack(R.id.onboardingLoaderfragment)
            navigate(
                    R.id.fragment_client_activation, bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to navFragmentsData?.getData()
                            ?.getString(StringConstants.JOB_PROFILE_ID.value),
                    StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value to true,
                    StringConstants.INVITE_USER_ID.value to navFragmentsData?.getData()
                            ?.getString(StringConstants.INVITE_USER_ID.value),
                    StringConstants.AUTO_REDIRECT_TO_APPL.value to navFragmentsData?.getData()?.getBoolean(
                        StringConstants.AUTO_REDIRECT_TO_APPL.value ,false)
            )

            )
            navFragmentsData?.getData()
                    ?.putBoolean(StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value, false)
            return true
        }
        return false
    }

    private fun setUserInCrashlytics(profile: ProfileData) {
        val username = profile?.name
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        var mobileNo = ""
        profile?.contact?.let {
            for (contact in it) {
                mobileNo += contact.phone + ','
            }
            if (mobileNo.contains(",")) {
                mobileNo.substring(0, mobileNo.length - 2)
            }

        }
        FirebaseCrashlytics.getInstance().setUserId(uid)
        FirebaseCrashlytics.getInstance().setCustomKey("username", username)
        FirebaseCrashlytics.getInstance().setCustomKey("mobileno", mobileNo)

    }

    private fun navigateToCalendarHomeScreen() {
        popFragmentFromStack(R.id.onboardingLoaderfragment)
        navigate(R.id.mainHomeScreen)
    }

}