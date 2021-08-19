package com.gigforce.client_activation.client_activation.repository

import androidx.lifecycle.MutableLiveData
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.verification.KYCdata
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AadhaarDetailsRepository @Inject constructor() : BaseFirestoreDBRepository() {

    var statesCollectionName = "Mst_States"
    var citiesCollectionName = "Mst_Cities"
    var verificationCollectionName = "Verification"
    var profileCollectionName = "Profiles"
    var COLLECTION_NAME = "Profiles"

    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    companion object {
        private const val COLLECTION_NAME = "Verification"
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun getStatesFromDb(): MutableList<State> {
        try {
            val await = db.collection(statesCollectionName).get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }
            val toObjects = await.toObjects(State::class.java)
            for (i in 0 until await.documents.size) {
                toObjects[i].id = await.documents[i].id
            }
            return toObjects
        } catch (e: Exception) {
            return mutableListOf()
        }

    }

    suspend fun getCities(stateCode: String): MutableList<City> {
        try {
            val await = db.collection(citiesCollectionName).whereEqualTo("state_code", stateCode).get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }
            val toObjects = await.toObjects(City::class.java)
            for (i in 0 until await.documents.size) {
                toObjects[i].id = await.documents[i].id
            }
            return toObjects
        } catch (e: Exception) {
            return mutableListOf()
        }
    }

    suspend fun getVerificationDetails(): VerificationBaseModel? {
        try {
            val await = db.collection(verificationCollectionName).document(uid).get().await()
            if (!await.exists()) {
                return VerificationBaseModel()
            }
            val toObject = await.toObject(VerificationBaseModel::class.java)
            return toObject
        } catch (e: Exception) {
            return VerificationBaseModel()
        }
    }

    suspend fun getAddressData(): AddressModel? {
        try {
            val await = db.collection(profileCollectionName).document(uid).get().await()
            if (!await.exists()) {
                return AddressModel()
            }
            val toObject = await.toObject(AddressModel::class.java)
            return toObject
        } catch (e: Exception) {
            return AddressModel()
        }
    }

}