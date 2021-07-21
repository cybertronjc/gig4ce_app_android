package com.gigforce.lead_management.ui.share_application_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareApplicationLinkViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
) : ViewModel() {

    companion object {
        private const val TAG = "ShareApplicationLinkViewModel"
    }

    init {
        getJobProfileForSharing()
    }

    private val _viewState = MutableLiveData<Lce<List<JobProfileOverview>>>()
    val viewState: LiveData<Lce<List<JobProfileOverview>>> = _viewState

    //Data
    private var jobProfiles: List<JobProfileOverview> = emptyList()
    private var jobProfilesShownOnView: List<JobProfileOverview> = emptyList()

    private fun getJobProfileForSharing() = viewModelScope.launch {
        _viewState.postValue(Lce.loading())

        try {
            logger.d(TAG, "fetching job profiles...")

            jobProfiles = leadManagementRepository.getJobProfiles(
                tlUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
            )
            jobProfilesShownOnView = jobProfiles
            _viewState.value = Lce.content(jobProfiles)

            logger.d(TAG, "received ${jobProfiles.size} job profiles from server")

        } catch (e: Exception) {
            _viewState.value = Lce.error("Unable to load Job Profiles")
            logger.e(
                TAG,
                " getJobProfileForSharing()",
                e
            )
        }
    }


    fun searchJobProfiles(
        searchString: String
    ) {
        logger.d(TAG, "search job profiles called , search string : '${searchString}'")

        if (searchString.isEmpty()) {
            jobProfilesShownOnView = jobProfiles
            _viewState.value = Lce.content(jobProfilesShownOnView)
            return
        }

        jobProfilesShownOnView = jobProfiles.filter {
            it.tradeName?.contains(searchString, true) ?: false
                    || it.profileName?.contains(searchString, true) ?: false
        }

        _viewState.value = Lce.content(jobProfilesShownOnView)
        logger.d(TAG, "Job profiles found after search : ${jobProfilesShownOnView.size}")
    }

}