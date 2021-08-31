package com.gigforce.client_activation.client_activation.repository

import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileRequestDataModel
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface JobProfileService {

    @POST
    suspend fun getJobProfiles(
        @Url getJobprofile : String,
        @Body request : JobProfileRequestDataModel
    ): Response<List<JobProfileDVM>>
}