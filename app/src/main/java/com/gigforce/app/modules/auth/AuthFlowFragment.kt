package com.gigforce.app.modules.auth

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.AppConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AllAuthenticationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuthFlowFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lang = getSharedData(AppConstants.APP_LANGUAGE, null)
        if (lang != null && lang.length > 0)
            updateResources(lang)

        val introComplete = getSharedData(AppConstants.INTRO_COMPLETE, null)


        popFragmentFromStack(R.id.authFlowFragment)
        if (lang == null) {
            navigate(R.id.languageSelectFragment)//, null, navOptionsPopToHome)
        } else if (introComplete == null) {
            navigate(R.id.introSlidesFragment)//, null, navOptionsPopToHome)
        } else {
            FirebaseAuth.getInstance().addAuthStateListener {
                onAuthStateChanged(it.currentUser)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_auth_flow, inflater, container)
    }

    private fun onAuthStateChanged(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            navigate(R.id.Login)
        } else {
            var fragments = getFragmentManager()?.getFragments()
            if (fragments != null && fragments?.size == 1) {
                val onboardingCompleted = getSharedData(AppConstants.ON_BOARDING_COMPLETED, null)
                if (onboardingCompleted == null || onboardingCompleted.equals("")||onboardingCompleted.equals("false"))
                    navigateWithAllPopupStack(R.id.onboardingfragment)
                else
                    navigateWithAllPopupStack(R.id.mainHomeScreen)
            } else {
                navigateWithAllPopupStack(R.id.loginSuccessfulFragment)
            }
        }
    }

}