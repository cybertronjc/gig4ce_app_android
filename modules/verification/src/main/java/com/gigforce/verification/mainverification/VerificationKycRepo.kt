package com.gigforce.verification.mainverification

import android.util.Log
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody

class VerificationKycRepo(private val iBuildConfigVM: IBuildConfigVM) :
    BaseFirestoreDBRepository() {
    private val kycService: VerificationKycService = RetrofitFactory.createService(
        VerificationKycService::class.java
    )

    suspend fun getVerificationOcrResult(
        type: String,
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

        var model = OCRQueryModel(type, "RAjCRVuaqaRhhM8qbwOaO97wo9x2", subType)
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

    suspend fun getKycVerification(type: String, list: List<Data>): KycOcrResultModel {
        Log.d("Here", type + " list " + list.toString())
        val kycVerifyReqModel = KycVerifyReqModel(type, "RAjCRVuaqaRhhM8qbwOaO97wo9x2", list)
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

    suspend fun setVerifiedStatus(status: Boolean?) : Boolean{
        try {
            db.collection(getCollectionName()).document(getUID()).updateOrThrow(
                mapOf(
                    "bank_details.verified" to status
                )
            )
            return true
        }catch (e: Exception){
            return false
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