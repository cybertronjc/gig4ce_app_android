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
import com.gigforce.app.modules.auth.ui.main.ProfileAnGigInfo
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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

    @Inject
    lateinit var shareDataAndCommUtil: SharedPreAndCommonUtilInterface

    private val firebaseAuthStateListener: FirebaseAuthStateListener by lazy {
        FirebaseAuthStateListener.getInstance()
    }

    private val SPLASH_TIME_OUT: Long = 250
    private var shouldCheckInJoinings = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.onboarding_loader_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginSuccessfulViewModel::class.java)
        getDataFrom(
            arguments,
            savedInstanceState
        )
        observer()
        viewModel.getProfileAndGigData()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            shouldCheckInJoinings =
                it.getBoolean(AppConstants.SHOULD_CHECK_FOR_JOININGS_APPLICATIONS)
        }

        savedInstanceState?.let {
            shouldCheckInJoinings =
                it.getBoolean(AppConstants.SHOULD_CHECK_FOR_JOININGS_APPLICATIONS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            AppConstants.SHOULD_CHECK_FOR_JOININGS_APPLICATIONS,
            shouldCheckInJoinings
        )
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
                            checkForPendingWritesAndVavigateToHomeScr(profileAndGig)
                        }
                    } else {
                        navigateToMainOnboarding()
                    }
                } else
                    showToast(profileAndGig.profile.errormsg)
            }
        })
    }

    private fun checkForPendingWritesAndVavigateToHomeScr(profileAndGig: ProfileAnGigInfo) {

        if (shouldCheckInJoinings) {
            checkForApplicationInvites(profileAndGig)
            return
        }

        checkPendingWritesAndNavigateNormally(profileAndGig)
    }
    var pendingWritesDoneOnce = false
    private fun checkPendingWritesAndNavigateNormally(profileAndGig: ProfileAnGigInfo) {

        FirebaseFirestore
            .getInstance()
            .waitForPendingWrites()
            .addOnSuccessListener {
                if(!pendingWritesDoneOnce) {
                    pendingWritesDoneOnce = true
                    CrashlyticsLogger.d(
                        "OnboardingLoggerFragment",
                        "Success no pending writes found"
                    )

                    if (profileAndGig.hasGigs) {
                        navigateToCalendarHomeScreen()
                    } else {
                        navigateToLandingHomeScreen()
                    }
                }
            }.addOnFailureListener {
                if(!pendingWritesDoneOnce) {
                    pendingWritesDoneOnce = true
                    CrashlyticsLogger.e(
                        "OnboardingLoggerFragment",
                        "while syncning data to server",
                        it
                    )

                    if (profileAndGig.hasGigs) {
                        navigateToCalendarHomeScreen()
                    } else {
                        navigateToLandingHomeScreen()
                    }
                }
            }.runCatching {
                if(!pendingWritesDoneOnce) {
                    pendingWritesDoneOnce = true
                    CrashlyticsLogger.e(
                        "OnboardingLoggerFragment",
                        "Internet connection not found"
                    )
                    if (profileAndGig.hasGigs) {
                        navigateToCalendarHomeScreen()
                    } else {
                        navigateToLandingHomeScreen()
                    }
                }
            }
    }

    private fun checkForApplicationInvites(profileAndGig: ProfileAnGigInfo) {
        FirebaseFirestore.getInstance()
            .collection("Joinings")
            .whereEqualTo(
                "phoneNumber",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().phoneNumber
            )
            .whereIn(
                "status",
                listOf(
                    "sign_up_pending",
                    "application_pending",
                )
            ).get()
            .addOnSuccessListener {

                if (it.isEmpty) {
                    checkPendingWritesAndNavigateNormally(profileAndGig)
                } else {
                    checkForPendingInvitesAndOpenJobProfile(
                        it,
                        profileAndGig
                        )
                }
            }
            .addOnFailureListener {
                checkPendingWritesAndNavigateNormally(profileAndGig)
            }
    }

    private fun checkForPendingInvitesAndOpenJobProfile(
        query: QuerySnapshot?,
        profileAndGig: ProfileAnGigInfo
    ) {
        val querySnap = query ?: return

        val joinings = querySnap.documents.map {

            it.toObject(Joining::class.java)!!.apply {
                this.joiningId = it.id
            }
        }

        for (joing in joinings) {
            if (!joing.jobProfileIdInvitedFor.isNullOrEmpty()) {

                popAllBackStates()
                navigate(
                    R.id.fragment_client_activation,
                    bundleOf(StringConstants.JOB_PROFILE_ID.value to joing.jobProfileIdInvitedFor)
                )
                return
            }
        }

        checkPendingWritesAndNavigateNormally(profileAndGig)
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
                    StringConstants.AUTO_REDIRECT_TO_APPL.value to navFragmentsData?.getData()
                        ?.getBoolean(
                            StringConstants.AUTO_REDIRECT_TO_APPL.value, false
                        )
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