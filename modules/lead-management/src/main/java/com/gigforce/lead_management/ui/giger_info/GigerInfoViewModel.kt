package com.gigforce.lead_management.ui.giger_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.gigforce.lead_management.ui.select_gig_application.SelectGigAppViewState
import com.gigforce.lead_management.ui.select_gig_location.SelectGigLocationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class GigerInfoViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GigerInfoViewModel"
    }

    private val _viewState = MutableLiveData<GigerInfoState>()
    val viewState: LiveData<GigerInfoState> = _viewState

    fun getGigerJoiningInfo() = viewModelScope.launch {
        _viewState.postValue(GigerInfoState.LoadingDataFromServer)

        try {
            logger.d(TAG, "fetching giger joining info...")

            val gigerJoiningDetails = leadManagementRepository.getGigerJoiningInfo(
            )
            _viewState.value = GigerInfoState.GigerInfoLoaded(gigerJoiningDetails)

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

}