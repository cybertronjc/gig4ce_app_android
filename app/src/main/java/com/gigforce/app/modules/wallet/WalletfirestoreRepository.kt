package com.gigforce.app.modules.wallet

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.wallet.models.Payslip
import com.gigforce.app.utils.getOrThrow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class WalletfirestoreRepository: BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Wallets"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun getPaySlips() : List<Payslip>{
        val querySnap = db.collection(COLLECTION_PAYSLIPS)
            .whereEqualTo("uid", getUID())
            .getOrThrow()

        return querySnap.documents.map {
            it.toObject(Payslip::class.java)!!
        }.sortedBy {
            it.getMonthNo()
        }
    }


    companion object{
        const val COLLECTION_PAYSLIPS = "Payslips"
    }
}