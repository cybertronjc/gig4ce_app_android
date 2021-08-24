package com.gigforce.lead_management.ui.assign_gig_dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lse
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignGigsViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
) : ViewModel() {

    companion object {
        private const val TAG = "AssignGigsViewModel"
    }

    private val _viewState = MutableLiveData<Lse>()
    val viewState: LiveData<Lse> = _viewState

    fun assignGigs(
        assignGigRequest: AssignGigRequest
    ) = viewModelScope.launch {
        logger.d(
            TAG,
            "Assigning gigs....",
        )

        try {
            _viewState.postValue(Lse.loading())

            assignGigRequest.enrollingTlUid =
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid

            if (!assignGigRequest.gigForceTeamLeaders.isNullOrEmpty())
                assignGigRequest.gigForceTeamLeaders?.first()?.primary = true

            if (!assignGigRequest.businessTeamLeaders.isNullOrEmpty())
                assignGigRequest.businessTeamLeaders?.first()?.primary = true

            logger.d(
                TAG,
                "Assigning gigs [Data]...., $assignGigRequest",
            )
            leadManagementRepository.assignGigs(
                assignGigRequest
            )
            _viewState.postValue(Lse.success())
            logger.d(
                TAG,
                "[Success] Gigs assigned",
                assignGigRequest
            )
        } catch (e: Exception) {
            _viewState.postValue(
                Lse.error(
                    "Unable to assign gigs, please try again later"
                )
            )

            logger.e(
                TAG,
                "[Failure] Gigs assign failed",
                e
            )
        }
    }
}