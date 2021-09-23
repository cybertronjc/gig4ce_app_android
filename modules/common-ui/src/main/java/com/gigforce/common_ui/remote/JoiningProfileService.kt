package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.leadManagement.*
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



    @GET("joining/listing")
    suspend fun getJoiningListing(

    ): Response<List<JoiningNew>>

    @GET("joining/detail/{id}")
    suspend fun getJoiningInfo(
        @Path("id") id: String
    ): Response<GigerInfo>

    @GET("business/listing/businessandjobProfile")
    suspend fun getBusinessAndJobProfiles(): Response<List<JoiningBusinessAndJobProfilesItem>>

    @GET("business/businessLocations")
    suspend fun getBusinessLocationAndTeamLeaders(
        @Query("businessId") businessId : String
    ): Response<JoiningLocationTeamLeadersShifts>

    @POST("joining/submit")
    suspend fun submitJoiningRequest(
        @Body joiningRequest: SubmitJoiningRequest
    ): Response<AssignGigResponse>
}