package com.gigforce.app.modules.gigerVerfication

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.DocumentReference

class GigerVerificationRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

    companion object {
        private const val COLLECTION_NAME = "Verification"
    }

    fun checkForSignedContract(): DocumentReference {
        return db.collection("Verification").document(getUID())
    }

    suspend fun updatePanInfo(
        userId: String? = null,
        userHasPanCard: Boolean,
        fileNameAtServer: String? = null,
        panCardNo: String? = null
    ) {
        //todo
        db.collection(getCollectionName()).document(
            userId ?: getUID()
        ).update(mapOf(
            "pan_card.userHasPanCard" to userHasPanCard,
            "pan_card.panCardImagePath" to fileNameAtServer,
            "pan_card.verified" to false,
            "pan_card.panCardNo" to panCardNo,
            "pan_card.state" to -1,
            "pan_card.verifiedString" to "Under Verification",
            "sync_status" to false
        ))
    }

    suspend fun updateAadharInfo(
        userId: String? = null,
        userHasAadharCard: Boolean,
        frontImagePathAtServer: String? = null,
        backImagePathAtServer: String? = null,
        aadharNo: String? = null
    ) {

        //todo
        db.collection(getCollectionName()).document(
            userId ?: getUID()
        ).update(mapOf(
            "aadhar_card.userHasAadharCard" to userHasAadharCard,
            "aadhar_card.frontImage" to frontImagePathAtServer,
            "aadhar_card.backImage" to backImagePathAtServer,
            "aadhar_card.verified" to false,
            "aadhar_card.aadharCardNo" to aadharNo,
            "aadhar_card.state" to -1,
            "aadhar_card.verifiedString" to "Under Verification",
            "sync_status" to false
        ))
    }

    suspend fun updateBankDetails(
        userId: String? = null,
        userHasPassBook: Boolean,
        passbookImagePath: String? = null,
        ifscCode: String? = null,
        bankName: String? = null,
        accountNo : String? = null
    ) {

        //todo
        db.collection(getCollectionName()).document(
            userId ?: getUID()
        ).update(mapOf(
            "bank_details.userHasPassBook" to userHasPassBook,
            "bank_details.passbookImagePath" to passbookImagePath,
            "bank_details.verified" to false,
            "bank_details.ifscCode" to ifscCode,
            "bank_details.bankName" to bankName,
            "bank_details.accountNo" to accountNo,
            "bank_details.state" to -1,
            "bank_details.verifiedString" to "Under Verification",
            "sync_status" to false
        ))
    }

    suspend fun updateDlDetails(
        userId: String? = null,
        userHasDL: Boolean,
        frontImageFileNameAtServer: String? = null,
        backImageFileNameAtServer: String? = null,
        dlState: String? = null,
        dlNo : String? = null
    ) {

        //todo
        db.collection(getCollectionName()).document(
            userId ?: getUID()
        ).update(mapOf(
            "driving_license.userHasDL" to userHasDL,
            "driving_license.frontImage" to frontImageFileNameAtServer,
            "driving_license.backImage" to backImageFileNameAtServer,
            "driving_license.dlState" to dlState,
            "driving_license.dlNo" to dlNo,
            "driving_license.state" to -1,
            "driving_license.verifiedString" to "Under Verification",
            "sync_status" to false
        ))
    }
}