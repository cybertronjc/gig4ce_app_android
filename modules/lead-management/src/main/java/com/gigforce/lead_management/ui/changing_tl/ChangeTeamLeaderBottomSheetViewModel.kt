package com.gigforce.lead_management.ui.changing_tl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.ext.asLiveData
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.ResultItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ChangeTeamLeaderBottomSheetState {

    object LoadingTeamLeaders : ChangeTeamLeaderBottomSheetState()

    data class TeamLeaderListLoaded(
        val teamLeaders: List<TeamLeader>
    ) : ChangeTeamLeaderBottomSheetState()

    data class ErrorLoadingTeamLeaders(
        val error: String
    ) : ChangeTeamLeaderBottomSheetState()

    object ChangingTl : ChangeTeamLeaderBottomSheetState()

    object TeamLeaderChangedForAllGigers : ChangeTeamLeaderBottomSheetState()

    data class SomeTeamLeaderChangeFailed(
        val failedList: List<ResultItem>
    ) : ChangeTeamLeaderBottomSheetState()

    data class ErrorWhileChangingTeamLeaders(
        val error: String
    ) : ChangeTeamLeaderBottomSheetState()
}

@HiltViewModel
class ChangeTeamLeaderBottomSheetViewModel @Inject constructor(
    private val leadManagmentRepository: LeadManagementRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        const val TAG = "ChangeTeamLeaderBottomSheetViewModel"
    }

    //data from view
    var gigerForChangeTL: List<ChangeTeamLeaderRequestItem> = emptyList()
    var sharedViewModel : LeadManagementSharedViewModel? = null

    private val _viewState = MutableLiveData<ChangeTeamLeaderBottomSheetState>()
    val viewState = _viewState.asLiveData()

    init {
        getTeamLeaders()
    }

    private fun getTeamLeaders() = viewModelScope.launch {
        _viewState.value = ChangeTeamLeaderBottomSheetState.LoadingTeamLeaders
        logger.d(
            TAG,
            "loading teamleaders..."
        )

        runCatching {
            leadManagmentRepository.getTeamLeadersForChangeTl()
        }.onSuccess {
            _viewState.value = ChangeTeamLeaderBottomSheetState.TeamLeaderListLoaded(it)
        }.onFailure {
            logger.e(
                TAG,
                "unable to fetch tl list",
                it
            )

            _viewState.value = ChangeTeamLeaderBottomSheetState.ErrorLoadingTeamLeaders(
                it.message ?: "Unable to load teamleaders"
            )
        }
    }

    fun changeTeamLeadersOfGigersTo(
        teamLeader: TeamLeader
    ) = viewModelScope.launch {
        _viewState.value = ChangeTeamLeaderBottomSheetState.ChangingTl

        runCatching {

            gigerForChangeTL.onEach {
                it.teamLeaderId = teamLeader.id!!
            }

            logger.d(
                TAG,
                "changing team leaders....",
                gigerForChangeTL
            )

            leadManagmentRepository.changeTeamLeadersOfGigers(
                ChangeTeamLeaderRequest(gigerForChangeTL)
            )
        }.onSuccess { changeTLResponse ->


            if (changeTLResponse.result.isNullOrEmpty()) {
                sharedViewModel?.joiningsChanged(emptyList()) //todo write logic for this one or shift to flow
                _viewState.value = ChangeTeamLeaderBottomSheetState.TeamLeaderChangedForAllGigers
            } else {
                sharedViewModel?.joiningsChanged(emptyList())

                logger.e(
                    TAG,
                    "teamleader change failed for some gigers",
                    Exception("team leader change failed : ${changeTLResponse.result}")
                )

                _viewState.value = ChangeTeamLeaderBottomSheetState.SomeTeamLeaderChangeFailed(
                    changeTLResponse.result!!
                )
            }
        }.onFailure {
            _viewState.value = ChangeTeamLeaderBottomSheetState.ErrorWhileChangingTeamLeaders(
                "Unable to change TL, please try again"
            )

            logger.e(
                TAG,
                "changing tls",
                it
            )
        }
    }

}