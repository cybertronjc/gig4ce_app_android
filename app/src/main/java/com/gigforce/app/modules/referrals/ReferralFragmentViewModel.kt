package com.gigforce.app.modules.referrals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.SingleLiveEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch

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

    fun getReferredPeople(profileIDs: List<String>) = viewModelScope.launch {
        if (profileIDs.isEmpty()) return@launch

        try {
            val referredPeoples = modelReference.getReferredPeople(profileIDs)
            observableReferredPeople.value = referredPeoples
        } catch (e: Exception) {
            observableReferralErr.value = e.message
        }
    }

    override fun referredPeopleResponse(
            querySnapshot: QuerySnapshot?,
            error: FirebaseFirestoreException?
    ) {
        if (error != null) {

            return
        }
        if (!querySnapshot?.documents.isNullOrEmpty())
            observableReferredPeople.value = querySnapshot?.toObjects(ProfileData::class.java)
    }


}