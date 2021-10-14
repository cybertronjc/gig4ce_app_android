package com.gigforce.client_activation.client_activation.repository

import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileRequestDataModel
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.ReferralService
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import javax.inject.Inject

class JobProfileRepository(private val iBuildConfigVM: IBuildConfigVM) : BaseFirestoreDBRepository()  {
    private val jobProfileService: JobProfileService = RetrofitFactory.createService(
        JobProfileService::class.java
    )
    suspend fun getJobProfiles(
        requestBody: JobProfileRequestDataModel
    ): List<JobProfileDVM> = jobProfileService.getJobProfiles(
        iBuildConfigVM.getBaseUrl() + "jobProfile/filterJobProfile",
        requestBody
    ).bodyOrThrow()

    override fun getCollectionName(): String {
        return "Job_Profiles"
    }
}