package com.gigforce.app.modules.wallet

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class WalletfirestoreRepository: BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Wallets"

    var db = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    fun getUserWallet(): DocumentReference {
        return db.collection(COLLECTION_NAME).document(uid)
    }

}