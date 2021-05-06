package com.gigforce.app.modules.explore_by_role

import android.location.Location
import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.client_activation.client_activation.models.Role
import com.gigforce.core.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ExploreByRoleViewModel(private val callbacks: ExploreByRoleCallbacks) : ViewModel(),
    ExploreByRoleCallbacks.ResponseCallbacks {
    private val _observerRoleList: SingleLiveEvent<ArrayList<Role>> by lazy {
        SingleLiveEvent<ArrayList<Role>>();
    }
    private val _observerMarkedAsInterest: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observerMarkedAsInterest: SingleLiveEvent<Boolean> get() = _observerMarkedAsInterest
    val observerRoleList: SingleLiveEvent<ArrayList<Role>> get() = _observerRoleList

    private val _observerError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observerError: SingleLiveEvent<String> get() = _observerError
    private val _observerVerified: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observerVerified: SingleLiveEvent<Boolean> get() = _observerVerified

    fun getRoles() {
        callbacks.getRoles(this)
    }

    override fun getRolesResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            observerError.value = error.message
            return
        }
        val roles = querySnapshot?.toObjects(Role::class.java)
        querySnapshot?.documents?.forEachIndexed { index, element ->
            roles?.get(index)?.id = element.id
        }
        val arrayList = ArrayList<Role>()
        arrayList.addAll(roles!!)
        observerRoleList.value = arrayList
    }

    override fun docsVerifiedResponse(
        querySnapshot: DocumentSnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error == null) {
            val obj = querySnapshot?.toObject(VerificationBaseModel::class.java)
            observerVerified.value =
                obj?.bank_details != null && obj?.bank_details?.verified ?: false && obj?.selfie_video != null && obj?.selfie_video?.verified ?: false
                        && obj?.pan_card != null && obj?.pan_card?.verified ?: false && obj?.aadhar_card != null && obj?.aadhar_card?.verified ?: false
                        && obj?.driving_license != null && obj?.driving_license?.verified ?: false
        } else {
            observerError.value = error?.message
        }
    }

    override fun markedAsInterestSuccess(it: Task<Void>) {
        if (it.isSuccessful) {
            observerMarkedAsInterest.value = true
        } else {
            observerError.value = it.exception?.message
        }
    }

    fun addAsInterest(
        roleID: String,
        location: Location?,
        inviteID: String
    ) {
        callbacks.markAsInterest(roleID, inviteID, location, this)

    }

    fun checkVerifiedDocs() {
        callbacks.checkIfDocsAreVerified(this)
    }


}