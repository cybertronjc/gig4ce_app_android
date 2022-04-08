package com.gigforce.giger_gigs.travelling_info

//import com.gigforce.core.base.models.BaseResponse
import com.gigforce.common_ui.viewdatamodels.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TravellingService {
    @GET("gigs/gigergigSummaryReport?")
    suspend fun getAllTravellingInfo(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        ): Response<ResponseObjectModel<TravellingResponseDM>>
}