package com.gigforce.wallet

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.utils.EventLogs.getOrThrow
import com.gigforce.wallet.models.Payslip
import javax.inject.Inject

class WalletfirestoreRepository @Inject constructor() : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Wallets"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun getPaySlips(): List<Payslip> {
        val querySnap = db.collection(COLLECTION_PAYSLIPS)
            .whereEqualTo("uid", getUID())
            .getOrThrow()

        return querySnap.documents.map {
            val slip = it.toObject(Payslip::class.java)!!
            slip.id = it.id
            slip
        }.sortedBy {
            it.getMonthNo()
        }
    }


    companion object {
        const val COLLECTION_PAYSLIPS = "Payslips"
    }
}