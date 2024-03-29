package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.common_ui.viewdatamodels.PendingJoiningItemDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.google.gson.JsonObject
import org.json.JSONObject
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
    suspend fun getJoiningListing(): Response<List<JoiningNew>>

    @GET("business/listing/businessandjobProfile")
    suspend fun getBusinessAndJobProfiles(): Response<List<JoiningBusinessAndJobProfilesItem>>

    @GET("business/businessLocations")
    suspend fun getBusinessLocationAndTeamLeaders(
        @Query("businessId") businessId : String,
        @Query("jobProfileId") jobProfileId : String
    ): Response<JoiningLocationTeamLeadersShifts>

    @POST("joining/submit")
    suspend fun submitJoiningRequest(
        @Body joiningRequest: SubmitJoiningRequest
    ): Response<AssignGigResponse>


    @GET("joining/detail/{id}")
    suspend fun getJoiningInfo(
        @Path("id") id: String,
        @Query("gigId") gigId : String?
    ): Response<GigerInfo>

    @GET("joining/pendingEjoining")
    suspend fun getPendingJoining(): Response<BaseResponse<PendingJoiningItemDVM>>

    @POST("joining/dropEjoining")
    suspend fun dropSelections(
        @Body jsonObject: DropSelectionRequest
    ): Response<DropSelectionResponse>

    @GET("business/listing/tlBusinessJobProfile")
    suspend fun getTeamLeadersForSelection(
        @Query("businessId") businessId: String,
        @Query("allTL") shouldFetchAllTeamLeaders : Boolean
    ): Response<GetTeamLeadersResponse>

    @GET("business/listing/teamLeadersToChange")
    suspend fun getTeamLeadersForChangeTL(): Response<GetTeamLeadersResponse>

    @POST("joining/changeTeamLeader")
    suspend fun changeTeamLeadersOfGigers(
        @Body changeTeamLeaderRequest : ChangeTeamLeaderRequest
    ): Response<ChangeTLResponse>

    @GET("earning/components/{businessId}")
    suspend fun getSalaryComponents(
        @Path("businessId") businessId: String,
        @Query("type") type: String,
    ): Response<InputSalaryResponse>
}