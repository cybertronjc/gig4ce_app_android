package com.gigforce.client_activation.client_activation.repository

import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.profile.PFESICDataModel
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PFESICDetailsRepository @Inject constructor() : BaseFirestoreDBRepository() {

    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    companion object {
        private const val COLLECTION_NAME = "Profiles"
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun getDataFromProfile(): PFESICDataModel? {
        try {
            val await = db.collection(getCollectionName()).document(uid).get().await()

            val toObject = await.toObject(PFESICDataModel::class.java)

            return toObject
        } catch (e: Exception) {
            return PFESICDataModel()
        }
    }

    suspend fun submitData(pfesicData: PFESICDataModel): Boolean {
        try {
            db.collection(getCollectionName()).document(uid).updateOrThrow(
                mapOf(
                    "pfesic" to pfesicData,
                    "updatedAt" to Timestamp.now(),
                    "updatedBy" to uid
                )
            )
            return true
        } catch (e: Exception) {
            return false
        }

    }
}