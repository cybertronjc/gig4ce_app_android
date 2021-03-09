package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.client_activation.client_activation.models.Role
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

class RoleDetailsVIewModel(private val callbacks: RoleDetailsCallbacks) : ViewModel(),
    RoleDetailsCallbacks.ResponseCallbacks {
    private var newUser: Boolean = false
    var openQuestionnaire: Boolean = false
    var emailValidated = false
    private val _observerRole: SingleLiveEvent<Role> by lazy {
        SingleLiveEvent<Role>();
    }
    val observerRole: SingleLiveEvent<Role> get() = _observerRole
    private val _observerDataToCheck: SingleLiveEvent<MutableList<Any>> by lazy {
        SingleLiveEvent<MutableList<Any>>();
    }
    val observerDataToCheck: SingleLiveEvent<MutableList<Any>> get() = _observerDataToCheck

    private val _observerMarkedAsInterest: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observerMarkedAsInterest: SingleLiveEvent<Boolean> get() = _observerMarkedAsInterest

    private val _observerError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observerError: SingleLiveEvent<String> get() = _observerError
    fun getRoleDetails(id: String?) {
        callbacks.getRoleDetails(id, this)
    }

    fun addAsInterest(roleID: String) {
        callbacks.markAsInterest(roleID, this)
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

    override fun markedAsInterestSuccess(it: Task<Void>) {
        if (it.isSuccessful) {
            observerMarkedAsInterest.value = true
        } else {
            observerError.value = it.exception?.message
        }
    }

    fun checkForProfileCompletionAndVerification() {
        callbacks.checkForProfileCompletionAndVerification(this)
    }

    fun getUID(): String {
        return callbacks.getUID()
    }

    override fun <T> checkDataResponse(data: T) {

        if (data is ProfileData) {
            dataCheckList.clear()
            dataCheckList.add(data)
        } else {
            dataCheckList.add(data!!)
        }
        if (dataCheckList.size == 2) {
            observerDataToCheck.value = dataCheckList

        }


    }

    val dataCheckList = mutableListOf<Any>()

    fun openQuestionnaireLandingAgain() {
        this.openQuestionnaire = true
    }

    fun setNewUser(b: Boolean) {
        this.newUser = b;
    }

    fun isNewUser(): Boolean {
        return newUser
    }
}