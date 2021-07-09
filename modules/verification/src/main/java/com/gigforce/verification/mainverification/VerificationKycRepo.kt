package com.gigforce.verification.mainverification

import android.util.Log
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody

class VerificationKycRepo(private val iBuildConfigVM: IBuildConfigVM) :
    BaseFirestoreDBRepository() {
    private val kycService: VerificationKycService = RetrofitFactory.createService(
        VerificationKycService::class.java
    )
    suspend fun getVerificationOcrResult(type: String, subType: String, image: MultipartBody.Part): KycOcrResultModel{
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", type)
        jsonObject.addProperty("uId", FirebaseAuth.getInstance().currentUser?.uid)
        jsonObject.addProperty("subType", subType)
        var kycOcrStatus = kycService.getKycOcrResult(iBuildConfigVM.getVerificationKycOcrResult(),jsonObject.toString(), image)
        if(kycOcrStatus.isSuccessful){
            Log.d("kycResult", kycOcrStatus.toString())
            return kycOcrStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : kycOcrVerification Method ${kycOcrStatus.message()}")
            throw Exception("Issue in KYC Ocr result ${kycOcrStatus.message()}")
            Log.d("kycResult", kycOcrStatus.toString())
        }
    }

    suspend fun getKycVerification(type: String, list: List<Data>): KycOcrResultModel{
        Log.d("Here", type + " list "+ list.toString())
        val kycVerifyReqModel = KycVerifyReqModel(type, getUID(), list)
        val kycOcrStatus = kycService.getKycVerificationService(iBuildConfigVM.getKycVerificationUrl(), kycVerifyReqModel)
        if(kycOcrStatus.isSuccessful){
            Log.d("kycResult", kycOcrStatus.toString())
            return kycOcrStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : kycOcrVerification Method ${kycOcrStatus.message()}")
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

    override fun getCollectionName(): String =
        COLLECTION_NAME


    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}