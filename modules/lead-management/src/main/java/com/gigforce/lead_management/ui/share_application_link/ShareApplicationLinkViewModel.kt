package com.gigforce.lead_management.ui.share_application_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigForGigerActivation
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.gigforce.lead_management.ui.joining_list.JoiningListViewState

class ShareApplicationLinkViewModel constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "ShareApplicationLinkViewModel"
    }

    private val _viewState = MutableLiveData<Lce<List<GigForGigerActivation>>>()
    val viewState: LiveData<Lce<List<GigForGigerActivation>>> = _viewState

}