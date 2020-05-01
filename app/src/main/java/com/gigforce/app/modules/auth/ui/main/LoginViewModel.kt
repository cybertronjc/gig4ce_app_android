package com.gigforce.app.modules.auth.ui.main

import android.app.Activity
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
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

    val liveState: MutableLiveData<Int> = MutableLiveData<Int>(STATE_INITIALIZED)
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
            liveState.postValue(STATE_VERIFY_SUCCESS)
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            liveState.value = STATE_VERIFY_FAILED
        }

        override fun onCodeSent(
            _verificationId: String,
            _token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(_verificationId, _token)
            verificationId = _verificationId
            token = _token
            liveState.postValue(STATE_CODE_SENT)
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
                    liveState.postValue(STATE_SIGNIN_SUCCESS)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", it.exception)
                    liveState.value = STATE_SIGNIN_FAILED
                }
            }
            .addOnSuccessListener {
                Log.d("status", "Signed in successfully")
            }
            .addOnFailureListener {
                Log.d("status", "Signed in failed")
            }
    }
}