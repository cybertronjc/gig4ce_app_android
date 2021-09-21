package com.gigforce.app.modules.auth

//import com.gigforce.app.utils.AppConstants
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.navigation.INavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFlowFragment : Fragment() {
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var shareDataAndCommUtil: SharedPreAndCommonUtilInterface
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lang = sharedPreAndCommonUtilInterface.getAppLanguageCode()
        if (lang != null && lang.isNotEmpty())
            sharedPreAndCommonUtilInterface.updateResources(lang)
        navigation.popBackStack("authFlowFragment", inclusive = true)
        when {
            lang.isNullOrBlank() -> {
                navigation.navigateTo("languageSelectFragment")
            }
            else -> {
                FirebaseAuth.getInstance().addAuthStateListener {
                    onAuthStateChanged(it.currentUser)
                }
            }
        }

    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth_flow, container, false)
    }

    private fun onAuthStateChanged(currentUser: FirebaseUser?) {

        currentUser?.let {

            navigation.popAllBackStates()
            sharedPreAndCommonUtilInterface.saveData(AppConstants.LANGUAGE_SELECTED, "true")
            eventTracker.setUserId(currentUser.phoneNumber.toString())
            eventTracker.setProfileProperty(ProfilePropArgs("Mobile Number", currentUser.phoneNumber.toString()))
            eventTracker.setUserProperty(mapOf("mobile_number" to currentUser.phoneNumber.toString(), "firebase_uid" to currentUser.uid.toString()))
            eventTracker.setProfileProperty(ProfilePropArgs("Firebase UID", currentUser.uid.toString()))
            eventTracker.setProfileProperty(ProfilePropArgs("Mobile Number", currentUser.phoneNumber.toString()))
            Log.d("navigate", "navigate to onboarding loader")
            navigation.popAllBackStates()
            navigation.navigateTo("loader_screen")
        } ?: run {
            navigation.popAllBackStates()
            navigation.navigateTo("login")

        }
    }

}