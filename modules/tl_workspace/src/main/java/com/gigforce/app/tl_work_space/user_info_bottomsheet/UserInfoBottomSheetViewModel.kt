package com.gigforce.app.tl_work_space.user_info_bottomsheet

import android.widget.TextView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info.UserInfoRepository
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoBottomSheetViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val savedStateHandle: SavedStateHandle,
    private val repository : UserInfoRepository
) : BaseViewModel<
        GigerInformationDetailsBottomSheetFragmentViewEvents,
        GigerInformationDetailsBottomSheetFragmentViewState,
        GigerInformationDetailsBottomSheetFragmentViewEffects>(initialState = GigerInformationDetailsBottomSheetFragmentViewState.LoadingGigerInformation) {

    private lateinit var openGigerDetailsFor: String
    private lateinit var gigerId: String
    private lateinit var jobProfileId: String
    private lateinit var businessId: String
    private var eJoiningId: String? = null
    private var payoutId: String? = null

    init {
        tryRestoringKeys()
    }

    private fun tryRestoringKeys() {
        gigerId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF
        ) ?: return
        jobProfileId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID
        ) ?: return
        businessId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID
        ) ?: return
        eJoiningId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_E_JOINING_ID
        )
        payoutId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_PAYOUT_ID
        )

        fetchUserDetails()
    }


    fun setKeysReceivedFromPreviousScreen(
        openDetailsFor: String,
        gigerId: String,
        businessId: String,
        jobProfileId: String,
        eJoiningId: String?,
        payoutId: String?
    ) {
        this.openGigerDetailsFor = openDetailsFor
        this.gigerId = gigerId
        this.businessId = businessId
        this.jobProfileId = jobProfileId
        this.eJoiningId = eJoiningId
        this.payoutId = payoutId
        fetchUserDetails()

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF,
            openGigerDetailsFor
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID,
            jobProfileId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID,
            businessId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_E_JOINING_ID,
            eJoiningId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_PAYOUT_ID,
            payoutId
        )
    }

    private fun fetchUserDetails() = viewModelScope.launch {

        setState {
            GigerInformationDetailsBottomSheetFragmentViewState.LoadingGigerInformation
        }

        try {
            repository.getUserInfo(
                fetchInfoFor = openGigerDetailsFor,
                gigerId = gigerId,
                jobProfileId = jobProfileId,
                businessId = businessId,
                payoutId = payoutId,
                eJoiningId = eJoiningId
            )
        } catch (e: Exception) {
        }
    }

    override fun handleEvent(
        event: GigerInformationDetailsBottomSheetFragmentViewEvents
    ) {
        when (event) {
            is GigerInformationDetailsBottomSheetFragmentViewEvents.ActionButtonClicked -> TODO()
        }
    }


}