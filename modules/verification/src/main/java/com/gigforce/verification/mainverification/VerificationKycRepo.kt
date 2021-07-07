package com.gigforce.verification.mainverification

import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import okhttp3.MultipartBody

class VerificationKycRepo(private val iBuildConfigVM: IBuildConfigVM) :
    BaseFirestoreDBRepository() {
    private val kycService: VerificationKycService = RetrofitFactory.createService(
        VerificationKycService::class.java
    )
    suspend fun getVerificationOcrResult(type: String, image: MultipartBody.Part):KYCImageRecogModel{
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", type)
        jsonObject.addProperty("uid", getUID())
        var userAuthStatus = kycService.getKycOcrResult(iBuildConfigVM.getVerificationKycOcrResult(),jsonObject, image)
        if(userAuthStatus.isSuccessful){
            return userAuthStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : checkIfSignInOrSignup Method ${userAuthStatus.message()}")
            throw Exception("Issue in Authentication result ${userAuthStatus.message()}")
        }
    }

    override fun getCollectionName(): String =
        COLLECTION_NAME


    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}