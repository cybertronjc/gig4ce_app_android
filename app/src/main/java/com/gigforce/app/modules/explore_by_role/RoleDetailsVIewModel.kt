package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.Role
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

class RoleDetailsVIewModel(private val callbacks: RoleDetailsCallbacks) : ViewModel(),
    RoleDetailsCallbacks.ResponseCallbacks {
    private val _observerRole: SingleLiveEvent<Role> by lazy {
        SingleLiveEvent<Role>();
    }
    val observerRole: SingleLiveEvent<Role> get() = _observerRole

    private val _observerError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observerError: SingleLiveEvent<String> get() = _observerError
    fun getRoleDetails(id: String?) {
        callbacks.getRoleDetails(id, this)
    }

    override fun getRoleDetailsResponse(
        querySnapshot: DocumentSnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            _observerError.value = error.message
            return
        }
        observerRole.value = querySnapshot?.toObject(Role::class.java)
    }
}