package com.gigforce.verification.mainverification

import android.util.Log
import com.gigforce.common_ui.remote.verification.Data
import com.gigforce.common_ui.remote.verification.KycVerifyReqModel
import com.gigforce.common_ui.remote.verification.*
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.common_ui.remote.verification.VaccineFileUploadResDM
import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.logger.GigforceLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import javax.inject.Inject

class VerificationKycRepo @Inject constructor(private val iBuildConfigVM: IBuildConfigVM, private val kycService : VerificationKycService, private val gigforceLogger: GigforceLogger) :
    BaseFirestoreDBRepository() {

    suspend fun getVerificationOcrResult(
        type: String,
        uid: String,
        subType: String,
        image: MultipartBody.Part
    ): KycOcrResultModel {
        val model = OCRQueryModel(type,uid, subType)
        val kycOcrStatus =
            kycService.getKycOcrResult(iBuildConfigVM.getVerificationKycOcrResult(), model, image)
        if (kycOcrStatus.isSuccessful) {
            Log.d("kycResult", kycOcrStatus.toString())
            return kycOcrStatus.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : kycOcrVerification Method ${kycOcrStatus.message()}")
            throw Exception("Issue in KYC Ocr result ${kycOcrStatus.message()}")
        }
    }

    suspend fun getKycVerification(type: String, list: List<Data>, uid: String): KycOcrResultModel {
        Log.d("Here", type + " list " + list.toString())
        val kycVerifyReqModel = KycVerifyReqModel(type, uid, list)
        val kycOcrStatus = kycService.getKycVerificationService(
            iBuildConfigVM.getKycVerificationUrl(),
            kycVerifyReqModel
        )
        if (kycOcrStatus.isSuccessful) {
            Log.d("kycResult", kycOcrStatus.toString())
            return kycOcrStatus.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : kycOcrVerification Method ${kycOcrStatus.message()}")
            throw Exception("Issue in KYC Ocr result ${kycOcrStatus.message()}")
        }
    }

    suspend fun setVerifiedStatus(status: Boolean, uid: String) : UserConsentResponse{
        try {
                val userConsentResponse = kycService.onConfirmButton(iBuildConfigVM.getKycUserConsentUrl(),UserConsentRequest(status))
                if(userConsentResponse.isSuccessful){
                    return userConsentResponse.body()!!
                }else{
                    FirebaseCrashlytics.getInstance()
                        .log("Exception : kycOcrVerification Method ${userConsentResponse.message()}")
                    throw Exception("Issue in KYC Ocr result ${userConsentResponse.message()}")
                }
        }catch (e: Exception){
            throw Exception("Issue in network call $e")
        }
    }

    suspend fun setUserAknowledge() : UserConsentResponse{
        try {
                val userConsentResponse = kycService.onConfirmButton(iBuildConfigVM.getKycUserConsentUrl(),UserConsentRequest(counter = 1))
                if(userConsentResponse.isSuccessful){
                    return userConsentResponse.body()!!
                }else{
                    FirebaseCrashlytics.getInstance()
                        .log("Exception : kycOcrVerification Method ${userConsentResponse.message()}")
                    throw Exception("Issue in KYC Ocr result ${userConsentResponse.message()}")
                }

        }catch (e: Exception){
            throw Exception("Issue in network call}")
        }
    }

    suspend fun setVerificationStatusStringToBlank(uid: String){
        try {
            db.collection(getCollectionName()).document(uid).updateOrThrow(
                mapOf(
                    "bank_details.status" to ""
                )
            )
        }catch (e: Exception){
        }
    }

    suspend fun getComplianceData(): List<ComplianceDocDetailsDM> {
        val complianceStatus = kycService.getComplianceData(
            iBuildConfigVM.getComplianceDataUrl()
        )
        if (complianceStatus.isSuccessful) {
            return complianceStatus.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : kycOcrVerification Method ${complianceStatus.message()}")
            throw Exception("Issue in KYC Ocr result ${complianceStatus.message()}")
        }
    }

    override fun getCollectionName(): String =
        COLLECTION_NAME

    suspend fun submitVaccinationCertificate(vaccineReqDM : VaccineIdLabelReqDM, file: MultipartBody.Part): VaccineFileUploadResDM {
        val vaccinationCertificate =
                    kycService.uploadVaccineCertificate(iBuildConfigVM.getBaseUrl()+"verificationVaccine/verfiyVaccineCertificate",
                        vaccineReqDM, file)
        if (vaccinationCertificate.isSuccessful) {
            return vaccinationCertificate.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : submitVaccinationCertificate Method ${vaccinationCertificate?.message()}")
            throw Exception("Issue in vaccination certification submission ${vaccinationCertificate?.errorBody()}")
        }
    }

    suspend fun getVaccinationObjectData(userIdToUse:String?=null) : DocumentSnapshot{
        userIdToUse?.let {
            return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(it).getOrThrow()
        }
        throw Exception("User id not found!!")
    }



    suspend fun confirmVaccinationData(vaccineId: String):BaseResponse<Any> {
        val confirmationDetails =
            kycService.confirmVaccinationData(iBuildConfigVM.getBaseUrl()+"verificationVaccine/confirmVaccine",
                Data1(data = VaccineIdLabelReqDM(vaccineId = vaccineId))
            )
        if (confirmationDetails.isSuccessful) {
            return confirmationDetails.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : submitVaccinationCertificate Method ${confirmationDetails.message()}")
            throw Exception("Issue in vaccination certification submission")
        }
    }


    companion object {
        private const val COLLECTION_NAME = "Verification"
    }


    // below is unused code

    suspend fun getVerificationStatus(): VerificationBaseModel? {
        try {
            var verifiedResult: VerificationBaseModel? = VerificationBaseModel()
            val status = db.collection(getCollectionName()).document(getUID()).get().await()

            if (status.exists()) {
                verifiedResult = status.toObject(VerificationBaseModel::class.java)
            }
            return verifiedResult
        }catch (e: Exception){
            return VerificationBaseModel()
        }

    }

    suspend fun getBeneficiaryName(): String? {
        try{
            var beneficiaryName: String? = ""
            db.collection(getCollectionName()).document(getUID()).get().addOnSuccessListener {
                it.let {
                    if (it.contains("bank_details")){
                        val doc = it.toObject(VerificationBaseModel::class.java)
                        beneficiaryName = doc?.bank_details?.bankBeneficiaryName
                    }
                }
            }
            return beneficiaryName
        } catch (e: Exception){
            return ""
        }
    }

    suspend fun getStatesFromDb(): MutableList<States> {
        try {
            val await = db.collection("Mst_States").get().await()
            if (await.documents.isNullOrEmpty()) {
                return mutableListOf()
            }
            val toObjects = await.toObjects(States::class.java)
            for (i in 0 until await.documents.size) {
                toObjects[i].id = await.documents[i].id
            }
            return toObjects
        } catch (e: Exception) {
            return mutableListOf()
        }

    }

}