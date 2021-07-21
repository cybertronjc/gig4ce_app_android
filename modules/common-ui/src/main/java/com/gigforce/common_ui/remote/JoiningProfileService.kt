package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface JoiningProfileService {

    @GET
    suspend fun getProfiles(
        @Url getProfilesUrl : String,
        @Query("tlUid") tlUid: String,
        @Query("userUid") userUid: String?
    ): Response<List<JobProfileOverview>>

    @GET
    suspend fun getProfileDetails(
        @Url getProfilesDetailsUrl : String,
        @Query("jobProfileId") jobProfileId: String,
        @Query("tlUid") tlUid: String,
        @Query("userUid") userUid: String
    ): Response<JobProfileDetails>

}