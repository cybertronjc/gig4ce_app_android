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
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import javax.inject.Inject

class VerificationKycRepo @Inject constructor(private val iBuildConfigVM: IBuildConfigVM, private val kycService : VerificationKycService) :
    BaseFirestoreDBRepository() {
//    private val kycService: VerificationKycService = RetrofitFactory.createService(
//        VerificationKycService::class.java
//    )

    suspend fun getVerificationOcrResult(
        type: String,
        uid: String,
        subType: String,
        image: MultipartBody.Part
    ): KycOcrResultModel {
//        val jsonObject = JsonObject()
//        jsonObject.addProperty("type", type)
//        jsonObject.addProperty(
//            "uId",
//            "RAjCRVuaqaRhhM8qbwOaO97wo9x2"
//        )//FirebaseAuth.getInstance().currentUser?.uid)
//        jsonObject.addProperty("subType", subType)

        var model = OCRQueryModel(type,uid, subType)
        var kycOcrStatus =
            kycService.getKycOcrResult(iBuildConfigVM.getVerificationKycOcrResult(), model, image)
        if (kycOcrStatus.isSuccessful) {
            Log.d("kycResult", kycOcrStatus.toString())
            return kycOcrStatus.body()!!
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Exception : kycOcrVerification Method ${kycOcrStatus.message()}")
            throw Exception("Issue in KYC Ocr result ${kycOcrStatus.message()}")
            Log.d("kycResult", kycOcrStatus.toString())
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
            Log.d("kycResult", kycOcrStatus.toString())
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

    suspend fun setVerifiedStatus(status: Boolean?, uid: String) : UserConsentResponse{
        try {
            status?.let {
                val userConsentResponse = kycService.onConfirmButton(iBuildConfigVM.getKycUserConsentUrl(),UserConsentRequest(it))
                if(userConsentResponse.isSuccessful){
                    return userConsentResponse.body()!!
                }else{
                    FirebaseCrashlytics.getInstance()
                        .log("Exception : kycOcrVerification Method ${userConsentResponse.message()}")
                    throw Exception("Issue in KYC Ocr result ${userConsentResponse.message()}")
                }
            }
//            db.collection(getCollectionName()).document(uid).updateOrThrow(
//                mapOf(
//                    "bank_details.verified" to status,
//                    "bank_details.verifiedOn" to Timestamp.now(),
//                    "updatedAt" to Timestamp.now(),
//                    "updatedBy" to FirebaseAuthStateListener.getInstance()
//                        .getCurrentSignInUserInfoOrThrow().uid
//                )
//            )
            return throw Exception("Issue in user input}")
        }catch (e: Exception){
            return throw Exception("Issue in network call ${e}")
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
            return throw Exception("Issue in network call}")
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


    override fun getCollectionName(): String =
        COLLECTION_NAME


    companion object {
        private const val COLLECTION_NAME = "Verification"
    }
}