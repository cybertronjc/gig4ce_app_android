package com.gigforce.client_activation.client_activation.repository

import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.repo.IAadhaarDetailsRepository
import com.gigforce.core.extensions.toFirebaseTimeStamp
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class AadhaarDetailsRepository @Inject constructor() : BaseFirestoreDBRepository(), IAadhaarDetailsRepository {

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

    override suspend fun getStatesFromDb(): MutableList<State> {
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

    override suspend fun getCities(stateCode: String): MutableList<City> {
        try {
            val await =
                    db.collection(citiesCollectionName).whereEqualTo("state_code", stateCode).get()
                            .await()
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

    override suspend fun getVerificationDetails(): VerificationBaseModel? {
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

    suspend fun setAadhaarDetails(uid: String, aadhaardetails: AadhaarDetailsDataModel): Boolean {
        try {
            db.collection(verificationCollectionName).document(uid).updateOrThrow(
                    mapOf(
                            "aadhaar_card_questionnaire.frontImagePath" to aadhaardetails.frontImagePath,
                            "aadhaar_card_questionnaire.backImagePath" to aadhaardetails.backImagePath,
                            "aadhaar_card_questionnaire.aadhaarCardNo" to aadhaardetails.aadhaarCardNo,
                            "aadhaar_card_questionnaire.dateOfBirth" to aadhaardetails.dateOfBirth,
                            "aadhaar_card_questionnaire.fName" to aadhaardetails.fName,
                            "aadhaar_card_questionnaire.addLine1" to aadhaardetails.addLine1,
                            "aadhaar_card_questionnaire.addLine2" to aadhaardetails.addLine2,
                            "aadhaar_card_questionnaire.state" to aadhaardetails.state,
                            "aadhaar_card_questionnaire.city" to aadhaardetails.city,
                            "aadhaar_card_questionnaire.pincode" to aadhaardetails.pincode,
                            "aadhaar_card_questionnaire.landmark" to aadhaardetails.landmark,
                            "aadhaar_card_questionnaire.currentAddSameAsParmanent" to aadhaardetails.currentAddSameAsParmanent,
                            "aadhaar_card_questionnaire.currentAddress" to aadhaardetails.currentAddress
                    )
            )
            //                "aadhaar_card_questionnaire" to aadhaardetails
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun setAadhaarFromVerificationModule(nomineeAsFather: Boolean, aadhaardetails: AadhaarDetailsDataModel): Boolean {
        try {
            var mapData = mapOf(
                    "aadhaar_card_questionnaire.frontImagePath" to aadhaardetails.frontImagePath,
                    "aadhaar_card_questionnaire.backImagePath" to aadhaardetails.backImagePath,
                    "aadhaar_card_questionnaire.name" to aadhaardetails.name,
                    "aadhaar_card_questionnaire.aadhaarCardNo" to aadhaardetails.aadhaarCardNo,
                    "aadhaar_card_questionnaire.dateOfBirth" to aadhaardetails.dateOfBirth,
                    "aadhaar_card_questionnaire.fName" to aadhaardetails.fName,
                    "aadhaar_card_questionnaire.addLine1" to aadhaardetails.addLine1,
                    "aadhaar_card_questionnaire.addLine2" to aadhaardetails.addLine2,
                    "aadhaar_card_questionnaire.state" to aadhaardetails.state,
                    "aadhaar_card_questionnaire.city" to aadhaardetails.city,
                    "aadhaar_card_questionnaire.pincode" to aadhaardetails.pincode,
                    "aadhaar_card_questionnaire.landmark" to aadhaardetails.landmark
            )

            db.collection(verificationCollectionName).document(uid).updateOrThrow(
                    mapData
            )
            if (nomineeAsFather)
                db.collection("Profiles").document(uid).updateOrThrow(mapOf(
                        "pfNominee" to "father"))

            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun setAadhaarDetailsFromJoiningForm(
            uid: String,
            aadhaardetails: AadhaarDetailsDataModel
    ): Boolean {
        try {
            db.collection(verificationCollectionName).document(uid).updateOrThrow(
                    mapOf(
                            "aadhaar_card_questionnaire.aadhaarCardNo" to aadhaardetails.aadhaarCardNo,
                            "aadhaar_card_questionnaire.dateOfBirth" to aadhaardetails.dateOfBirth,
                            "aadhaar_card_questionnaire.fName" to aadhaardetails.fName,
                            "aadhaar_card_questionnaire.addLine1" to aadhaardetails.addLine1,
                            "aadhaar_card_questionnaire.addLine2" to aadhaardetails.addLine2,
                            "aadhaar_card_questionnaire.state" to aadhaardetails.state,
                            "aadhaar_card_questionnaire.city" to aadhaardetails.city,
                            "aadhaar_card_questionnaire.currentAddSameAsParmanent" to aadhaardetails.currentAddSameAsParmanent,
                            "aadhaar_card_questionnaire.currentAddress.addLine1" to aadhaardetails.currentAddress?.addLine1,
                            "aadhaar_card_questionnaire.currentAddress.addLine2" to aadhaardetails.currentAddress?.addLine2,
                            "aadhaar_card_questionnaire.currentAddress.state" to aadhaardetails.currentAddress?.state,
                            "aadhaar_card_questionnaire.currentAddress.city" to aadhaardetails.currentAddress?.city
                    )
            )
            //                "aadhaar_card_questionnaire" to aadhaardetails
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun setProfileRelatedData(
            uid: String,
            email: String,
            dateOfBirth: Date,
            fName: String,
            maritalStatus: String,
            emergencyContact: String
    ): Boolean {
        return try {
            db.collection("Profiles").document(uid).updateOrThrow(
                    mapOf(
                            "email" to email,
                            "dateOfBirth" to dateOfBirth.toFirebaseTimeStamp(),
                            "fName" to fName,
                            "maritalStatus" to maritalStatus,
                            "emergencyContact" to emergencyContact
                    )
            )
            true
        } catch (e: Exception) {
            false
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