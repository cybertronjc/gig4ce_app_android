package com.gigforce.giger_gigs.repositories

import android.util.Log
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigResponse
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.tl_login_details.LoginSummaryService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TlLoginSummaryRepository (
    private val buildConfig: IBuildConfigVM
) {
    companion object {
        private const val COLLECTION_PROFILE = "Profiles"
        const val COLLECTION_GIGS = "Gigs"
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
        val businessByCity = loginSummaryService.getBusinessByCityWithLoginCount(cityId)

        if (!businessByCity.isSuccessful){
            throw Exception(businessByCity.message())
        } else {
            return businessByCity.body()!!
        }
    }

    suspend fun submitLoginSummary(addNewSummaryReqModel: AddNewSummaryReqModel): Response<ResponseBody> {

        return loginSummaryService.submitLoginSummary(buildConfig.getListingBaseUrl() + "/submit" ,addNewSummaryReqModel)

    }

    suspend fun fetchListingForTL(page: Int, pageSize: Int): List<ListingTLModel> {
        val response = loginSummaryService.getListingForTL(buildConfig.getListingBaseUrl() + "/listingForTL/"+userUid, page, pageSize)

        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response.body()!!
        }

    }

    suspend fun checkIfAttendanceMarked() : CheckMark {
       val response = loginSummaryService.checkIfTLMarked(buildConfig.getListingBaseUrl() + "/gigerPresent/"+userUid)
        if (!response.isSuccessful){
            throw Exception(response.message())
        } else {
            return response.body()!!
        }
    }

    suspend fun submitLoginReport(
        addNewSummaryReqModel: List<DailyTlAttendanceReport>
    ): AssignGigResponse {

        try {
            val response = loginSummaryService.submitLoginReport(
                buildConfig.getBaseUrl() + "tlDailyReport/submit" ,
                addNewSummaryReqModel.first()
            )

            if(response.isSuccessful){
                return response.body()!!
            } else if(response.code() == 400){
                throw Exception(
                    response.errorBody()?.string() ?: "Unable to submit record"
                )
            } else {
                throw Exception("Unable to Submit record, please try again later")
            }
        } catch (e : Exception )
        {
            when(e) {
                is UnknownHostException, is SocketTimeoutException, is IOException -> {
                    throw Exception("Unable to connect to server")
                }
                else -> throw e
            }

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