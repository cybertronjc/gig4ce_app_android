package com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfoRepository @Inject constructor(
    private val service: UserInfoRetrofitService
) {

    suspend fun getUserInfo(
        fetchInfoFor: String,
        gigerId: String,
        jobProfileId: String,
        businessId: String,
        payoutId: String?,
        eJoiningId: String?
    ): GigerInfoApiModel {
        return service.getRetentionData(
            GigerInfoRequest(
                requiredData = fetchInfoFor,
                gigerId = gigerId,
                businessId = businessId,
                jobProfileId = jobProfileId,
                payoutId = payoutId,
                eJoiningId = eJoiningId
            )
        ).bodyFromBaseResponseElseThrow()
    }

}