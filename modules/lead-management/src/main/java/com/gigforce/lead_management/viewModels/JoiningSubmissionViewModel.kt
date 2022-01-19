package com.gigforce.lead_management.viewModels

import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.logger.GigforceLogger


open class JoiningSubmissionViewModel constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "JoiningSubmissionViewModel"
    }

    suspend fun cleanUpJoiningDataAndSubmitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) : String
    {
        //Cleaning up Final JSON
        joiningRequest.business.jobProfiles = emptyList()
        joiningRequest.jobProfile.dynamicFields = emptyList()
        joiningRequest.jobProfile.verificationRelatedFields = emptyList()

       return submitJoiningData(
            joiningRequest
        )
    }

    private suspend fun submitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) : String {

         logger.d(
             TAG,
             "Assigning gigs....",
         )

         try {
             logger.d(
                 TAG,
                 "Assigning gigs [Data]...., $joiningRequest",
             )

             val shareLink = try {
                 leadManagementRepository.createJobProfileReferralLink(joiningRequest.jobProfile.id!!)
             } catch (e: Exception) {
                 logger.d(TAG, "error while creating job profile share link", e)
                 ""
             }

             joiningRequest.shareLink = shareLink
             leadManagementRepository.submitJoiningRequest(
                 joiningRequest
             )

             logger.d(
                 TAG,
                 "[Success] Gigs assigned"
             )

             return shareLink
         } catch (e: Exception) {

             logger.e(
                 TAG,
                 "[Failure] Gigs assign failed",
                 e
             )
             throw e
         }
    }
}