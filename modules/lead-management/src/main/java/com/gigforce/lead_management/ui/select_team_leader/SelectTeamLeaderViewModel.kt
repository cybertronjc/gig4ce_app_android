package com.gigforce.lead_management.ui.select_team_leader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectTeamLeaderViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : ViewModel() {

    companion object {
        private const val TAG = "SelectTeamLeaderViewModel"
    }

    init {
        fetchTeamLeaders(false)
    }

    private val _viewState = MutableLiveData<Lce<List<TeamLeader>>>()
    val viewState: LiveData<Lce<List<TeamLeader>>> = _viewState

    var selectedTlID : String? =null
    var fetchingAllTeamLeader : Boolean =false

    fun fetchTeamLeaders(
        shouldFetchAllTeamLeaders : Boolean
    ) = viewModelScope.launch {

        logger.d(TAG,"fetching team-leaders, shouldFetchAllTeamLeaders : $shouldFetchAllTeamLeaders")

        try {
            val teamLeaders = leadManagementRepository.getTeamLeadersForSelection(
                shouldFetchAllTeamLeaders
            )

            logger.d(TAG,"[Success] fetched ${teamLeaders.size} team leaders")
            checkAndSelectPreviousSelectedUserAndEmit(teamLeaders)
            fetchingAllTeamLeader = shouldFetchAllTeamLeaders

        } catch (e: Exception) {

            logger.e(TAG,"[Error] while fetching team leaders",e)
            _viewState.value = Lce.error(
                "Unable to fetch team leaders"
            )
        }
    }

    private fun checkAndSelectPreviousSelectedUserAndEmit(teamLeaders: List<TeamLeader>) {
        if(teamLeaders.isEmpty()){
            _viewState.value = Lce.content(
                teamLeaders
            )
            return
        }

        if(selectedTlID != null){
            teamLeaders.find { it.isTeamLeaderEqual(selectedTlID!!) }?.selected = true
        } else {
            val currentTLUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid

            teamLeaders.onEach {

                if(it.isTeamLeaderEqual(currentTLUid)){
                    it.selected = true
                    selectedTlID = currentTLUid
                }
            }
        }


        _viewState.value = Lce.content(
            teamLeaders
        )
    }

}