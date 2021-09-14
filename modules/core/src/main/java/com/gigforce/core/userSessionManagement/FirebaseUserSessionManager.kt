package com.gigforce.core.userSessionManagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

sealed class FirebaseAuthUserState {
    data class UserSignedIn(
            val user: FirebaseUser
    ) : FirebaseAuthUserState()

    object UserSignedOut : FirebaseAuthUserState()

    object UserUnknown : FirebaseAuthUserState()
}

/**
 * Use getInstance() to use this class
 */
class FirebaseAuthStateListener private constructor() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val _authState: MutableLiveData<FirebaseAuthUserState> = MutableLiveData()
    val authState: LiveData<FirebaseAuthUserState> = _authState

    private var currentLoggedInUser: FirebaseUser? = null

    init {
        _authState.value = FirebaseAuthUserState.UserUnknown

        firebaseAuth.addAuthStateListener {

            currentLoggedInUser = it.currentUser
            if (currentLoggedInUser != null) {

                _authState.value = FirebaseAuthUserState.UserSignedIn(
                        user = currentLoggedInUser!!
                )
            } else {

                _authState.value = FirebaseAuthUserState.UserSignedOut
            }
        }
    }

    fun getCurrentSignInInfo(): FirebaseUser? {
        return getLoggedUserfromCurrentUser() ?: currentLoggedInUser
    }

    fun getCurrentSignInUserInfoOrThrow(): FirebaseUser {

        return getLoggedUserfromCurrentUser()
                ?: (currentLoggedInUser
                        ?: throw IllegalStateException("logged in user is null, check if any user is logged in or not"))
    }

    private fun getLoggedUserfromCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    companion object {

        private var firebaseAuthStateListener: FirebaseAuthStateListener? = null

        @Synchronized
        fun getInstance(): FirebaseAuthStateListener {
            return if (firebaseAuthStateListener != null) {
                firebaseAuthStateListener!!
            } else {
                firebaseAuthStateListener = FirebaseAuthStateListener()
                firebaseAuthStateListener!!
            }
        }
    }
}