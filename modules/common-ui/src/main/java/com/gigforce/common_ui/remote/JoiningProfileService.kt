package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.*

interface JoiningProfileService {

    @GET("gigs/jobprofiles")
    suspend fun getProfiles(
        @Query("tlUid") tlUid: String,
        @Query("userUid") userUid: String?
    ): Response<List<JobProfileOverview>>

    @GET
    suspend fun getProfileDetails(
        @Query("jobProfileId") jobProfileId: String,
        @Query("tlUid") tlUid: String,
        @Query("userUid") userUid: String
    ): Response<JobProfileDetails>

    @POST("gig/submit")
    suspend fun createGigs(
        @Body request : AssignGigRequest
    ): Response<String>

}