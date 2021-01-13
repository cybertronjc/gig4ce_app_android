package com.gigforce.app.modules.auth.ui.main

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*
import java.util.concurrent.TimeUnit

class LoginViewModel() : ViewModel() {

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

    init {
        FirebaseAuth.getInstance().currentUser.let {
            // this.liveState.postValue(STATE_SIGNIN_SUCCESS)
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            liveState.postValue(LoginResponse(STATE_VERIFY_SUCCESS, ""))
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            liveState.postValue(LoginResponse(STATE_VERIFY_FAILED, e.toString()))
        }

        override fun onCodeSent(
                _verificationId: String,
                _token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(_verificationId, _token)
            verificationId = _verificationId
            token = _token
            liveState.postValue(LoginResponse(STATE_CODE_SENT, ""))
        }
    }


    fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                activity!!, // Activity (for callback binding)
                callbacks // OnVerificationStateChangedCallbacks
        ) // ForceResendingToken from callbacks
    }


    fun verifyPhoneNumberWithCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                        liveState.postValue(LoginResponse(STATE_SIGNIN_FAILED, ""))
                    }
                }
                .addOnSuccessListener {
                    registerFirebaseToken()
                    Log.d(TAG, "Signed in successfully")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Signed in failed")
                    liveState.postValue(LoginResponse(STATE_SIGNIN_FAILED, it.toString()))
                }
    }

    private fun registerFirebaseToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                val token = it.result?.token
                if (it.isSuccessful) {
                    registerTokenOnServer(currentUser.uid, token!!)
                } else {
                    liveState.postValue(LoginResponse(STATE_SIGNIN_SUCCESS, ""))
                }
            }
        } else {
            liveState.postValue(LoginResponse(STATE_SIGNIN_SUCCESS, ""))
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