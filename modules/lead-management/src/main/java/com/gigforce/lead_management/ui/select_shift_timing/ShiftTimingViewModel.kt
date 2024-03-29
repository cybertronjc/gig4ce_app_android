package com.gigforce.lead_management.ui.select_shift_timing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.repository.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftTimingViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
) : ViewModel() {

    companion object {
        private const val TAG = "SelectGigLocationViewModel"
    }

    private val _viewState = MutableLiveData<Lce<JobProfileDetails>>()
    val viewState: LiveData<Lce<JobProfileDetails>> = _viewState

    fun getJobProfileDetails(jobProfileId: String, userUid: String) = viewModelScope.launch {
        _viewState.postValue(Lce.loading())

        try {
            logger.d(TAG, "fetching job profile details...")

            val jobProfileDetails = leadManagementRepository.getJobProfileDetails(
                jobProfileId,
                tlUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                userUid
            )
            _viewState.value = Lce.content(jobProfileDetails)

            logger.d(TAG, "received ${jobProfileDetails} job profiles from server")

        } catch (e: Exception) {
            _viewState.value = Lce.error("Unable to load Job Profile details")
            logger.e(
                TAG,
                " getJobProfileDetails()",
                e
            )
        }
    }

}