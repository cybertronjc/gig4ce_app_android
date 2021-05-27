package com.gigforce.app.modules.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFlowFragment : BaseFragment() {
    @Inject
    lateinit var eventTracker : IEventTracker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isLanguageSelected = getData(AppConstants.LANGUAGE_SELECTED)
        val lang = getAppLanguageCode()
        if (lang != null && lang.isNotEmpty())
            updateResources(lang)

        val introComplete = getIntroCompleted()


        popFragmentFromStack(R.id.authFlowFragment)
        when {
            lang.isNullOrBlank() -> {
                navigate(
                    R.id.languageSelectFragment
                )//, null, navOptionsPopToHome)
            }
//            introComplete.isNullOrBlank() -> {
//                navigate(
//                    R.id.introSlidesFragment
//                )//, null, navOptionsPopToHome)
//            }
            else -> {
//                popFragmentFromStack(R.id.authFlowFragment)
                FirebaseAuth.getInstance().addAuthStateListener {
                    onAuthStateChanged(it.currentUser)
                }
            }
        }

    }

    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
        return false
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//    return inflater.inflate(R.layout.fragment_auth_flow,container,false)
        return inflateView(R.layout.fragment_auth_flow, inflater, container)
    }

    private fun onAuthStateChanged(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            navigate(
                R.id.Login
            )
        } else {
            var fragments = fragmentManager?.fragments
//            if (fragments != null && fragments?.size == 1) {
            popAllBackStates()
            saveData(AppConstants.LANGUAGE_SELECTED, "true")
            eventTracker.setUserId(currentUser?.phoneNumber.toString());
            eventTracker.setProfileProperty(ProfilePropArgs("Mobile Number", currentUser?.phoneNumber.toString()))
            eventTracker.setUserProperty(mapOf("mobile_number" to currentUser?.phoneNumber.toString(), "firebase_uid" to currentUser?.uid.toString()))
            eventTracker.setProfileProperty(ProfilePropArgs("Firebase UID", currentUser?.uid.toString()))
            Log.d("navigate", "navigate to onboarding loader")

            navigateWithAllPopupStack(
                R.id.onboardingLoaderfragment

            )
//                val onboardingCompleted = isOnBoardingCompleted()
//                if (!onboardingCompleted!!) {
//                    navigateWithAllPopupStack(R.id.onboardingfragment)
//                }
//                else
//                    navigateWithAllPopupStack(R.id.landinghomefragment)
//            } else {
//                navigateWithAllPopupStack(R.id.loginSuccessfulFragment)
//            }
        }

    }

}