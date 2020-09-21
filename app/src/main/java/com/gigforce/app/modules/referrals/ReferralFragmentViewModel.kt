package com.gigforce.app.modules.referrals

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ReferralFragmentViewModel(private val modelReference: DataCallbacksReferralFragment) :
    ViewModel(), DataCallbacksReferralFragment.ResponseCallbacks {

    private val _observableReferredPeople: SingleLiveEvent<List<ProfileData>> by lazy {
        SingleLiveEvent<List<ProfileData>>();
    }
    val observableReferredPeople: SingleLiveEvent<List<ProfileData>> get() = _observableReferredPeople
    private val _observableReferralErr: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableReferralErr: SingleLiveEvent<String> get() = _observableReferralErr

    fun getReferredPeople(profileIDs: List<String>) {
        if (profileIDs.isEmpty()) return
        modelReference.getReferredPeople(this, profileIDs)
    }

    override fun referredPeopleResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            observableReferralErr.value = error.message
            return
        }
        if (!querySnapshot?.documents.isNullOrEmpty())
            observableReferredPeople.value = querySnapshot?.toObjects(ProfileData::class.java)
    }


}