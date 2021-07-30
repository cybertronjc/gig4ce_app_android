package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigResponse
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

    @GET("gigs/jobprofiles/{jobProfileId}")
    suspend fun getProfileDetails(
        @Path("jobProfileId") jobProfileId: String,
        @Query("tlUid") tlUid: String,
        @Query("userUid") userUid: String
    ): Response<JobProfileDetails>

    @POST("gigs/activations")
    suspend fun createGigs(
        @Body request : AssignGigRequest
    ): Response<AssignGigResponse>

}