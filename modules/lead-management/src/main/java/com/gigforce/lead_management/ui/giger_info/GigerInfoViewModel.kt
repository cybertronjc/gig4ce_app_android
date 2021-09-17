package com.gigforce.lead_management.ui.giger_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.gigforce.lead_management.ui.select_gig_application.SelectGigAppViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class GigerInfoState {

    object LoadingDataFromServer : GigerInfoState()

    data class ErrorLoadingData(
        val error: String
    ): GigerInfoState()

    data class GigerInfoLoaded(
        val gigApps: List<ApplicationChecklistRecyclerItemData>
    ) : GigerInfoState()
}

@HiltViewModel
class GigerInfoViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val buildConfig: IBuildConfigVM,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GigerInfoViewModel"
    }

    private val _viewState = MutableLiveData<GigerInfoState>()
    val viewState: LiveData<GigerInfoState> = _viewState



}