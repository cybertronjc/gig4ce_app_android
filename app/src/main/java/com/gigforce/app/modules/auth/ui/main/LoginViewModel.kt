package com.gigforce.app.modules.auth.ui.main

//import com.gigforce.app.modules.profile.models.ProfileData
//import com.gigforce.app.utils.getOrThrow
import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.auth.UserAuthRepo
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.IEventTracker
import com.gigforce.core.StringConstants
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.analytics.AuthEvents
import com.gigforce.core.datamodels.login.LoginResponse
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
        private val eventTracker: IEventTracker,
        private val iBuildConfigVM: IBuildConfigVM
) : ViewModel() {

    companion object {
        private const val TAG = "login/viewmodel"
        const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        const val STATE_INITIALIZED = 1
        const val STATE_CODE_SENT = 2
        const val STATE_VERIFY_FAILED = 3
        const val STATE_VERIFY_SUCCESS = 4
        const val STATE_SIGNIN_FAILED = 5
        const val STATE_SIGNIN_SUCCESS = 6
    }

    val userAuthRepo = UserAuthRepo(iBuildConfigVM)

    //    val liveState: MutableLiveData<Int> = MutableLiveData<Int>(STATE_INITIALIZED)
    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()

    var verificationId: String? = null
    var token: PhoneAuthProvider.ForceResendingToken? = null
    var activity: Activity? = null

    //    private var userProfile: ProfileData? = null
    var userAuthStatus: UserAuthStatusModel? = null

    init {
        FirebaseAuth.getInstance().currentUser.let {
            // this.liveState.postValue(STATE_SIGNIN_SUCCESS)
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            liveState.postValue(
                    LoginResponse(
                            STATE_VERIFY_SUCCESS,
                            ""
                    )
            )
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

//            if (userProfile == null) {
//                val errorMap = mapOf("Error" to e.toString())
//                eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_ERROR, errorMap))
//            } else {
//                val errorMap = mapOf("Error" to e.toString())
//                eventTracker.pushEvent(TrackingEventArgs(AuthEvents.LOGIN_ERROR, errorMap))
//            }
            userAuthStatus?.let {
                if (it.status)
                    eventTracker.pushEvent(
                            TrackingEventArgs(
                                    if (it.isUserRegistered) AuthEvents.LOGIN_ERROR else AuthEvents.SIGN_UP_ERROR,
                                    mapOf("Error" to e.toString())
                            )
                    )
            }

            liveState.postValue(LoginResponse(STATE_VERIFY_FAILED, e.toString()))
        }

        override fun onCodeSent(
                _verificationId: String,
                _token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(_verificationId, _token)
            verificationId = _verificationId
            token = _token
            liveState.postValue(
                    LoginResponse(
                            STATE_CODE_SENT,
                            ""
                    )
            )
        }
    }


    fun sendVerificationCode(phoneNumber: String, isResendCall: Boolean = false) =
            viewModelScope.launch {
                userAuthStatus = try {
                    userAuthRepo.getUserAuthStatus(phoneNumber.substring(3,phoneNumber.length))
                } catch (e: Exception) {
                    UserAuthStatusModel(
                        status = false
                    )
                }
                if (userAuthStatus?.status == true) {

                    if (userAuthStatus?.isUserRegistered == true) {

                        eventTracker.pushEvent(
                                TrackingEventArgs(
                                        eventName = if (isResendCall) AuthEvents.LOGIN_RESEND_OTP else AuthEvents.LOGIN_STARTED,
                                        props = if (isResendCall) null else mapOf(
                                                "phone_no" to phoneNumber
                                        )
                                )
                        )
                    } else {
                        eventTracker.pushEvent(
                                TrackingEventArgs(
                                        eventName = if (isResendCall) AuthEvents.SIGN_RESEND_OTP else AuthEvents.SIGN_UP_STARTED,
                                        props = if (isResendCall) null else mapOf(
                                                "phone_no" to phoneNumber
                                        )
                                )
                        )
                    }
                }
                else{
                    TrackingEventArgs(
                        eventName = AuthEvents.LOGIN_SIGNUP_STARTED_API_ERROR,
                        props = if (isResendCall) null else mapOf(
                            "phone_no" to phoneNumber
                        )
                    )
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber, // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        activity!!, // Activity (for callback binding)
                        callbacks // OnVerificationStateChangedCallbacks
                ) // ForceResendingToken from callbacks
            }


    fun verifyPhoneNumberWithCode(
            code: String,
            phoneNumber: String
    ) = viewModelScope.launch {

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.d(TAG, "signInWithCredential:success")
                        Log.e("eventtesting","capturing")
                    if (it.additionalUserInfo!!.isNewUser) {
                        eventTracker.pushEvent(
                                TrackingEventArgs(
                                        eventName = AuthEvents.SIGN_SUCCESS,
                                        props = null
                                )
                        )
                        updateRegisterStatusToDB()
                    } else {
                        checkIfSignInOrSignup() // User can be enrolled by ambassador or by using portal. so need to get detail from profile collection
                    }

                    registerFirebaseToken()
                }
                .addOnFailureListener {

                    handleSignInError(it)
                }
    }

    private fun checkIfSignInOrSignup() {

        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseFirestore
                    .getInstance()
                    .collection("Profiles").document(it.uid).get().addOnSuccessListener {
                        if (it.exists()) {
                            val profileData = it.toObject(ProfileData::class.java)
                                    ?: throw  IllegalStateException("unable to parse profile object")
                            if (profileData.isUserRegistered) {
                                eventTracker.pushEvent(
                                        TrackingEventArgs(
                                                eventName = AuthEvents.LOGIN_SUCCESS,
                                                props = null
                                        )
                                )
                            } else {
                                eventTracker.pushEvent(
                                        TrackingEventArgs(
                                                eventName = AuthEvents.SIGN_SUCCESS,
                                                props = null
                                        )
                                )
                                //code for updating register status
                                updateRegisterStatusToDB()

                            }
                            updateTermsAcceptedToDB()

                        } else {
                            eventTracker.pushEvent(
                                    TrackingEventArgs(
                                            eventName = AuthEvents.SIGN_SUCCESS,
                                            props = null
                                    )
                            )
                            updateRegisterStatusToDB()
                            updateTermsAcceptedToDB()

                        }
                    }.addOnFailureListener { exception ->
                        FirebaseCrashlytics.getInstance().log("Exception : checkIfSignInOrSignup Method $exception")
                    }
        }

    }

    private fun updateTermsAcceptedToDB() {
        FirebaseAuth.getInstance().currentUser?.let { it ->
            FirebaseFirestore
                .getInstance()
                .collection("Profiles").document(it.uid).update(mapOf("termsAccepted" to true, "updatedAt" to Timestamp.now(), "updatedBy" to it.uid)).addOnFailureListener { exception ->
                    FirebaseCrashlytics.getInstance().log("Exception : updateTermsAcceptedToDB Method $exception")
                }
        }
    }

    private fun updateRegisterStatusToDB() {
        FirebaseAuth.getInstance().currentUser?.let { it ->
            FirebaseFirestore
                    .getInstance()
                    .collection("Profiles").document(it.uid).update(mapOf("isUserRegistered" to true, "updatedAt" to Timestamp.now(), "updatedBy" to it.uid)).addOnFailureListener { exception ->
                        FirebaseCrashlytics.getInstance().log("Exception : updateRegisterStatusToDB Method $exception")
                    }
        }
    }

    private fun handleSignInError(it: Exception) =
            viewModelScope.launch {

                userAuthStatus?.let { userAuthModel ->
                    if (userAuthModel.status)
                        eventTracker.pushEvent(
                                TrackingEventArgs(
                                        if (userAuthModel.isUserRegistered) AuthEvents.LOGIN_ERROR else AuthEvents.SIGN_UP_ERROR,
                                        mapOf("Error" to it.toString())
                                )
                        )
                }
                liveState.postValue(LoginResponse(STATE_SIGNIN_FAILED, it.toString()))
            }

    private fun registerFirebaseToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (it.isSuccessful) {
                    val token = it.result?.token
                    registerTokenOnServer(currentUser.uid, token!!)
                } else {
                    liveState.postValue(
                            LoginResponse(
                                    STATE_SIGNIN_SUCCESS,
                                    ""
                            )
                    )
                }
            }
        } else {
            liveState.postValue(
                    LoginResponse(
                            STATE_SIGNIN_SUCCESS,
                            ""
                    )
            )
        }
    }

    private fun registerTokenOnServer(uid: String, fcmToken: String) {
        FirebaseFirestore.getInstance().collection("firebase_tokens")
                .document(fcmToken)
                .set(
                        hashMapOf(
                                "uid" to uid,
                                "type" to "fcm",
                                "timestamp" to Date().time,
                                "updatedAt" to Timestamp.now(),
                                "updatedBy" to uid,
                                "createdAt" to Timestamp.now()
                        )
                ).addOnSuccessListener {
                    Log.v(TAG, "Token Updated on Firestore Successfully")
                    liveState.postValue(LoginResponse(STATE_SIGNIN_SUCCESS, ""))
                }.addOnFailureListener {
                    Log.e(TAG, "Token Update Failed on Firestore", it)
                    liveState.postValue(LoginResponse(STATE_SIGNIN_SUCCESS, ""))
                }
    }
}