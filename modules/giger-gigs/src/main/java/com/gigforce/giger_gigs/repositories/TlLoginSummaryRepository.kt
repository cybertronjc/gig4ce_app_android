package com.gigforce.giger_gigs.repositories

import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.tl_login_details.LoginSummaryService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.ResponseBody
import retrofit2.Response

class TlLoginSummaryRepository (
    private val buildConfig: IBuildConfigVM
) {
    companion object {
        private const val COLLECTION_PROFILE = "Profiles"
    }
    private val loginSummaryService: LoginSummaryService = RetrofitFactory.createService(
        LoginSummaryService::class.java
    )
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userUid = FirebaseAuth.getInstance().uid

    private val profileCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection(COLLECTION_PROFILE)
    }

    suspend fun getCities(): List<LoginSummaryCity> {
        val loginSummaryCity = loginSummaryService.getLoginSummaryCities(buildConfig.getListingBaseUrl() + "/cities")

        if (!loginSummaryCity.isSuccessful){
            throw Exception(loginSummaryCity.message())
        } else {
            return loginSummaryCity.body()!!
        }
    }

    suspend fun getBusinessByCity(cityId: String): List<LoginSummaryBusiness> {
        val businessByCity = loginSummaryService.getBusinessByCity(buildConfig.getListingBaseUrl() + "/businessByCity/"+cityId)

        if (!businessByCity.isSuccessful){
            throw Exception(businessByCity.message())
        } else {
            return businessByCity.body()!!
        }
    }

    suspend fun submitLoginSummary(addNewSummaryReqModel: AddNewSummaryReqModel): Response<ResponseBody> {
        val response = loginSummaryService.submitLoginSummary(buildConfig.getListingBaseUrl() + "/submit" ,addNewSummaryReqModel)

        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response!!
        }
    }

    suspend fun submitLoginReport(
        addNewSummaryReqModel: List<DailyTlAttendanceReport>
    ) {

        addNewSummaryReqModel.forEach {

            val response = loginSummaryService.submitLoginReport(
                buildConfig.getBaseUrl() + "tlDailyReport/submit" ,
                it
            )

            if (!response.isSuccessful){
                throw Exception(response.message())
            }
        }
    }

    suspend fun fetchListingForTL(searchCity: String,searchDate: String,page: Int, pageSize: Int): List<ListingTLModel> {
        val response = loginSummaryService.getListingForTL(buildConfig.getListingBaseUrl() + "/listingForTL/"+userUid, searchCity, searchDate, page, pageSize)

        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response.body()!!
        }
    }

    suspend fun fetchTLDailyLoginReportListingForTL(searchCity: String,searchDate: String,page: Int, pageSize: Int): List<DailyLoginReport> {
        val response = loginSummaryService.getDailyLoginReportListingForTL(
            buildConfig.getBaseUrl() + "tlDailyReport/listingForTL/" + userUid,
            searchCity,
            searchDate,
            page,
            pageSize
        )

        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response.body()!!
        }
    }

}