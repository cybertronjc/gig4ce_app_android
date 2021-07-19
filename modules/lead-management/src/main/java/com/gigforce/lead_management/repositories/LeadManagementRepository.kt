package com.gigforce.lead_management.repositories

import com.gigforce.common_ui.viewdatamodels.leadManagement.GigForGigerActivation
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class LeadManagementRepository {

    companion object {
        private const val COLLECTION_JOININGS = "Joinings"
    }

    private val firebaseFirestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val joiningsCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_JOININGS)
    }

    private val firebaseAuthStateListener: FirebaseAuthStateListener by lazy {
        FirebaseAuthStateListener.getInstance()
    }

    suspend fun fetchJoinings(): List<Joining> =
        joiningsCollectionRef
            .whereEqualTo(
                "joiningTLUid",
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
            .getOrThrow()
            .toObjects(Joining::class.java)


    suspend fun getGigsForReferral(): List<GigForGigerActivation> {

        return emptyList()
    }

    suspend fun saveReference(
        userUid: String,
        name: String,
        relation: String,
        contactNo: String
    ) {


    }


}