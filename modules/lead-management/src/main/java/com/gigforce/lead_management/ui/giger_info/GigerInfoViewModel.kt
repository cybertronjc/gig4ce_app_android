package com.gigforce.lead_management.ui.giger_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GigerInfoState {

    object LoadingDataFromServer : GigerInfoState()

    data class ErrorLoadingData(
        val error: String
    ): GigerInfoState()

    data class GigerInfoLoaded(
        val gigerInfo: GigerInfo
    ) : GigerInfoState()
}

sealed class GigerInfoEffects {

    data class OpenChangeTeamLeaderScreen(
        val joiningId: String,
        val gigerId : String?,
        val gigerName : String?,
        val teamLeaderId : String
    ) : GigerInfoEffects()
}

@HiltViewModel
class GigerInfoViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val profileFirebaseRepository: ProfileFirebaseRepository,
    private val authStateListener: FirebaseAuthStateListener
) : ViewModel() {

    companion object {
        private const val TAG = "GigerInfoViewModel"
    }

    private var gigerInfo : GigerInfo?  = null
    private var currentJoiningId : String? = null

    private val _viewState = MutableLiveData<GigerInfoState>()
    val viewState: LiveData<GigerInfoState> = _viewState

    private val _viewEffects = MutableSharedFlow<GigerInfoEffects>()
    val viewEffects = _viewEffects.asSharedFlow()

    fun getGigerJoiningInfo(
        joiningId: String?,
        gigId : String?
    ) = viewModelScope.launch {
        _viewState.postValue(GigerInfoState.LoadingDataFromServer)

        try {
            logger.d(TAG, "fetching giger joining info...")

            currentJoiningId = joiningId
            val gigerJoiningDetails = leadManagementRepository.getGigerJoiningInfo(
                joiningId,
                gigId
            )
            gigerInfo = gigerJoiningDetails
            //currentJoiningId = gigerJoiningDetails.gigerId

            if (gigerJoiningDetails.message.isNullOrBlank())
                _viewState.value = GigerInfoState.GigerInfoLoaded(gigerJoiningDetails)
             else
                _viewState.value = GigerInfoState.ErrorLoadingData(gigerJoiningDetails.message.toString())

            logger.d(TAG, "received ${gigerJoiningDetails} giger joining info from server")

        } catch (e: Exception) {
            _viewState.value = GigerInfoState.ErrorLoadingData("Unable to load giger joining info")
            logger.e(
                TAG,
                " getGigerJoiningInfo()",
                e
            )
        }
    }


    fun openChangeTeamLeaderScreen() = viewModelScope.launch{
        val joiningInfo = gigerInfo ?: return@launch

        _viewEffects.emit(
            GigerInfoEffects.OpenChangeTeamLeaderScreen(
                joiningId = currentJoiningId!!,
                gigerId = joiningInfo.gigerId,
                gigerName = joiningInfo.gigerName,
                teamLeaderId = authStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
        )
    }

}