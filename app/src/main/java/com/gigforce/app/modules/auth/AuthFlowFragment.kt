package com.gigforce.app.modules.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.AppConstants
//import com.gigforce.app.utils.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFlowFragment : BaseFragment() {
    @Inject
    lateinit var eventTracker : IEventTracker
    @Inject
    lateinit var shareDataAndCommUtil : SharedPreAndCommonUtilInterface
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isLanguageSelected = getData(AppConstants.LANGUAGE_SELECTED)
        val lang = getAppLanguageCode()
        if (lang != null && lang.isNotEmpty())
            updateResources(lang)

        popFragmentFromStack(R.id.authFlowFragment)
        when {
            lang.isNullOrBlank() -> {
                navigate(
                    R.id.languageSelectFragment
                )
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
        return inflateView(R.layout.fragment_auth_flow, inflater, container)
    }

    private fun onAuthStateChanged(currentUser: FirebaseUser?) {

        currentUser?.let {

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
        }?: run {
            navigate(
                R.id.Login
            )

        }
    }

}