package com.gigforce.giger_gigs.repositories

import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.models.AddNewSummaryReqModel
import com.gigforce.giger_gigs.models.LoginSummaryBusiness
import com.gigforce.giger_gigs.models.LoginSummaryCity
import com.gigforce.giger_gigs.tl_login_details.LoginSummaryService
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class TlLoginSummaryRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val loginSummaryService: LoginSummaryService,
    private val buildConfig: IBuildConfig
) {
    companion object {
        private const val COLLECTION_PROFILE = "Profiles"
    }

    private val profileCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_PROFILE)
    }

    suspend fun getCities(): List<LoginSummaryCity> {
        val loginSummaryCity = loginSummaryService.getLoginSummaryCities()

        if (!loginSummaryCity.isSuccessful){
            throw Exception(loginSummaryCity.message())
        } else {
            return loginSummaryCity.body()!!
        }
    }

    suspend fun getBusinessByCity(cityId: String): List<LoginSummaryBusiness> {
        val businessByCity = loginSummaryService.getBusinessByCity(cityId = cityId)

        if (!businessByCity.isSuccessful){
            throw Exception(businessByCity.message())
        } else {
            return businessByCity.body()!!
        }
    }

    suspend fun submitLoginSummary(addNewSummaryReqModel: AddNewSummaryReqModel): Response<ResponseBody> {
        val response = loginSummaryService.submitLoginSummary(addNewSummaryReqModel)

        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response!!
        }
    }

}