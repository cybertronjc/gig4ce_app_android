package com.gigforce.giger_gigs.travelling_info

import com.gigforce.core.base.models.BaseResponse
import javax.inject.Inject

class TravellingRepository @Inject constructor(val travellingService: TravellingService) {

    suspend fun getAllTravellingInfo(fromDate: String,toDate: String): TravellingResponseDM? {
        val travellingDetailData = travellingService.getAllTravellingInfo(fromDate,toDate)
        if(travellingDetailData.isSuccessful) {
            return travellingDetailData.body()?.data
        }else
        return null
    }

}