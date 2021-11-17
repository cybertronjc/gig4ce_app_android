package com.gigforce.ambassador.user_rollment.kycdocs

import android.util.Log
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.client_activation.States
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody

class VerificationKycRepo(private val iBuildConfigVM: IBuildConfigVM)  {
    private var firebaseDB = FirebaseFirestore.getInstance()
    val db: FirebaseFirestore get() = firebaseDB
    private val kycService: VerificationKycService = RetrofitFactory.createService(
        VerificationKycService::class.java
    )

    suspend fun getVerificationOcrResult(
        uid: String,
        type: String,
        subType: String,
        image: MultipartBody.Part
    ): KycOcrResultModel {
        var model = OCRQueryModel(type, uid, subType)
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

    suspend fun getKycVerification(uid:String,type: String, list: List<Data>): KycOcrResultModel {
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


    suspend fun setVerifiedStatus(uid: String,status: Boolean?): Boolean {
        try {
            db.collection(getCollectionName()).document(uid).updateOrThrow(
                mapOf(
                    "bank_details.verified" to status,
                    "bank_details.verifiedOn" to Timestamp.now()
                )
            )
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun setVerificationStatusStringToBlank(uid: String) {
        try {
            db.collection(getCollectionName()).document(uid).updateOrThrow(
                mapOf(
                    "bank_details.status" to ""
                )
            )
        } catch (e: Exception) {
        }
    }


    fun getCollectionName(): String =
        COLLECTION_NAME


    companion object {
        private const val COLLECTION_NAME = "Verification"
    }
}