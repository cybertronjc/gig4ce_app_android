package com.gigforce.app.modules.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.StringConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class AuthFlowFragment : BaseFragment() {

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            introComplete.isNullOrBlank() -> {
                navigate(
                    R.id.introSlidesFragment
                )//, null, navOptionsPopToHome)
            }
            else -> {
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
            var fragments = getFragmentManager()?.getFragments()
            if (fragments != null && fragments?.size == 1) {
                popAllBackStates()
                navigateWithAllPopupStack(
                    R.id.onboardingLoaderfragment
                )
//                val onboardingCompleted = isOnBoardingCompleted()
//                if (!onboardingCompleted!!) {
//                    navigateWithAllPopupStack(R.id.onboardingfragment)
//                }
//                else
//                    navigateWithAllPopupStack(R.id.landinghomefragment)
            } else {
                navigateWithAllPopupStack(R.id.loginSuccessfulFragment)
            }
        }
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener!!)

    }

}