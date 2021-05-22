package com.gigforce.client_activation.client_activation.repository

import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ViewApplicationRepository: BaseFirestoreDBRepository() {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!


    var COLLECTION_NAME = "JP_Applications"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    fun getJobApplication(profileId: String): Query {
        val query = firebaseDB.collection(COLLECTION_NAME)
            .whereEqualTo("jpid", profileId)
            .whereEqualTo("gigerId", FirebaseAuth.getInstance().currentUser?.uid)

        return query
    }



}