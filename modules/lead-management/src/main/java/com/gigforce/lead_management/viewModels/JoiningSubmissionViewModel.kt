package com.gigforce.lead_management.viewModels

import android.content.Context
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.ValidationHelper
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.lead_management.R
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1ViewModel
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


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