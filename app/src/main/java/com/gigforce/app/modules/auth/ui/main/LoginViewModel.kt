package com.gigforce.app.modules.auth.ui.main

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.login.LoginResponse
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.analytics.AuthEvents
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.getOrThrow
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val eventTracker: IEventTracker
) : ViewModel() {

    companion object {
        private const val TAG = "login/viewmodel"
        public const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        public const val STATE_INITIALIZED = 1
        public const val STATE_CODE_SENT = 2
        public const val STATE_VERIFY_FAILED = 3
        public const val STATE_VERIFY_SUCCESS = 4
        public const val STATE_SIGNIN_FAILED = 5
        public const val STATE_SIGNIN_SUCCESS = 6
    }

    //    val liveState: MutableLiveData<Int> = MutableLiveData<Int>(STATE_INITIALIZED)
    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()

    var verificationId: String? = null
    var token: PhoneAuthProvider.ForceResendingToken? = null
    var activity: Activity? = null
    private var userProfile: ProfileData? = null


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

            if (userProfile == null) {
                val errorMap = mapOf("Error" to e.toString())
                eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_ERROR, errorMap))
            } else {
                val errorMap = mapOf("Error" to e.toString())
                eventTracker.pushEvent(TrackingEventArgs(AuthEvents.LOGIN_ERROR, errorMap))
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

            if (userProfile == null) {

                val docRef = FirebaseFirestore
                    .getInstance()
                    .collection("Profiles")
                    .whereEqualTo("loginMobile", phoneNumber)
                    .getOrThrow()

                if (docRef.size() > 0) {
                    userProfile = docRef.documents.get(0).toObject(ProfileData::class.java)
                }
            }

            if (userProfile != null) {

                if (isResendCall) {

                    eventTracker.pushEvent(
                        TrackingEventArgs(eventName = AuthEvents.LOGIN_RESEND_OTP, props = null)
                    )
                } else {
                    val props = mapOf(
                        "phone_no" to phoneNumber
                    )

                    eventTracker.pushEvent(
                        TrackingEventArgs(eventName = AuthEvents.LOGIN_STARTED, props = props)
                    )
                }
            } else {

                if (isResendCall) {

                    eventTracker.pushEvent(
                        TrackingEventArgs(eventName = AuthEvents.SIGN_RESEND_OTP, props = null)
                    )
                } else {
                    val props = mapOf(
                        "phone_no" to phoneNumber
                    )

                    eventTracker.pushEvent(
                        TrackingEventArgs(eventName = AuthEvents.SIGN_UP_STARTED, props = props)
                    )
                }
            }

//        val phoneNumberOptions = PhoneAuthOptions.newBuilder()
//                .setPhoneNumber(phoneNumber)
//                .setActivity(activity!!)
//                .requireSmsValidation(true)
//                .setTimeout(60, TimeUnit.SECONDS)
//                .setCallbacks(callbacks)
//                .build()
//
//        PhoneAuthProvider.verifyPhoneNumber(phoneNumberOptions)
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
    )  = viewModelScope.launch{

        if (userProfile == null) {

            val docRef = FirebaseFirestore
                .getInstance()
                .collection("Profiles")
                .whereEqualTo("loginMobile", phoneNumber)
                .getOrThrow()

            if (docRef.size() > 0) {
                userProfile = docRef.documents[0].toObject(ProfileData::class.java)
            }
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d(TAG, "signInWithCredential:success")

                if (it.additionalUserInfo!!.isNewUser) {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = AuthEvents.SIGN_SUCCESS,
                            props = null
                        )
                    )
                } else {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = AuthEvents.LOGIN_SUCCESS,
                            props = null
                        )
                    )
                }

                registerFirebaseToken()
            }
            .addOnFailureListener {

                handleSignInError(credential,it)
            }
    }

    private fun handleSignInError(credential: PhoneAuthCredential, it: Exception)  = viewModelScope.launch{

        if (userProfile == null) {

            val docRef = FirebaseFirestore
                .getInstance()
                .collection("Profiles")
                .whereEqualTo("loginMobile", credential.zzc())
                .getOrThrow()

            if (docRef.size() > 0) {
                userProfile = docRef.documents[0].toObject(ProfileData::class.java)
            }
        }

        if (userProfile == null) {
            val errorMap = mapOf("Error" to it.message!!)
            eventTracker.pushEvent(TrackingEventArgs(AuthEvents.SIGN_UP_ERROR, errorMap))
        } else {
            val errorMap = mapOf("Error" to it.message!!)
            eventTracker.pushEvent(TrackingEventArgs(AuthEvents.LOGIN_ERROR, errorMap))
        }

        Log.w(TAG, "signInWithCredential:failure", it)
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
                    "timestamp" to Date().time
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