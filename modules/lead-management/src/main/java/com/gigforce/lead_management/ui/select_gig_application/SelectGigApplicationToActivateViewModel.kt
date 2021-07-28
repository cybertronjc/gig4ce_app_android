package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobLocation
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SelectGigAppViewState {

    object LoadingDataFromServer : SelectGigAppViewState()

    object NoGigAppsFound : SelectGigAppViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : SelectGigAppViewState()

    data class GigAppListLoaded(
        val gigApps: List<GigAppListRecyclerItemData>
    ) : SelectGigAppViewState()

    object FetchingDataToStartJoiningProcess : SelectGigAppViewState()

    data class StartGigerJoiningProcess(
        val gigerInfo: GigerProfileCardDVM,
        val assignGigRequest: AssignGigRequest
    ) : SelectGigAppViewState()

    data class ErrorInStartingJoiningProcess(
        val error: String
    ) : SelectGigAppViewState()
}

@HiltViewModel
class SelectGigApplicationToActivateViewModel @Inject constructor(
    private val leadManagementRepo: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SelectGigApplicationViewModel"
    }

    private val _viewState = MutableLiveData<SelectGigAppViewState>()
    val viewState: LiveData<SelectGigAppViewState> = _viewState

    //Data
    private var gigAppListShownOnView: MutableList<GigAppListRecyclerItemData> = mutableListOf()
    private var currentSearchString: String? = null

    //Data
    private var jobProfiles: List<JobProfileOverview> = emptyList()
    private var jobProfilesShownOnView: List<JobProfileOverview> = emptyList()
    val gigAppsListForView = mutableListOf<GigAppListRecyclerItemData>()
    private var currentlySelectedGigIndex: Int = -1
    private val _selectedIndex = MutableLiveData<Int>()
    val selectedIndex: LiveData<Int> = _selectedIndex

    private val _selectedJobProfileOverview = MutableLiveData<JobProfileOverview>()
    val selectedJobProfileOverview: LiveData<JobProfileOverview> = _selectedJobProfileOverview


    fun getJobProfilesToActivate(userUid: String) = viewModelScope.launch {
        _viewState.postValue(SelectGigAppViewState.LoadingDataFromServer)
        _selectedIndex.value = -1
        try {
            logger.d(TAG, "fetching job profiles...")

            jobProfiles = leadManagementRepo.getJobProfilesWithStatus(
                tlUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid,
                userUid = userUid
            )
            jobProfilesShownOnView = jobProfiles
            _viewState.value = SelectGigAppViewState.GigAppListLoaded(
                gigAppListShownOnView
            )
            processGigApps(jobProfilesShownOnView)
            logger.d(TAG, "received ${jobProfiles}")
            logger.d(TAG, "received ${jobProfiles.size} job profiles from server")

        } catch (e: Exception) {
            _viewState.value = SelectGigAppViewState.ErrorInLoadingDataFromServer(
                error = e.message ?: "Unable to fetch gigApps",
                shouldShowErrorButton = true
            )
            logger.e(
                TAG,
                " getJobProfileForSharing()",
                e
            )
        }
    }

    private fun processGigApps(jobApps: List<JobProfileOverview>) {

        val gigAppList: List<JobProfileOverview> = jobApps
        val statusToGigAppList = gigAppList.filter {
//            if (currentSearchString.isNullOrEmpty())
//                true
//            else  {
//                it.ongoing == false && it.tradeName?.contains(
//                    currentSearchString!!,
//                    true
//                ) ?: false
//                        || it.ongoing == false && it.profileName?.contains(
//                    currentSearchString!!,
//                    true
//                ) ?: false
//            }
            it.ongoing != null
        }.groupBy {
            if (!it.ongoing) "Ongoing Applications" else "Other Applications"
        }

        gigAppsListForView.clear()
        try {
            if (jobApps.isEmpty()) {
                gigAppsListForView.add(
                    GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData(
                        "Other Applications"
                    )
                )
                gigAppsListForView.add(
                    GigAppListRecyclerItemData.NoGigAppsFoundItemData(
                        "No Applications"
                    )
                )
            } else if (jobApps.isEmpty()) {
                gigAppsListForView.add(
                    GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData(
                        "Ongoing Applications"
                    )
                )
                gigAppsListForView.add(
                    GigAppListRecyclerItemData.NoGigAppsFoundItemData(
                        "No Applications"
                    )
                )
            }

            statusToGigAppList.forEach { (ongoing, gigApps) ->
                logger.d(TAG, "processing data, Status :  : ${statusToGigAppList.size} GigApps")

                gigAppsListForView.add(
                    GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData(
                        "$ongoing"
                    )
                )

                if (ongoing.equals("Other Applications")) {
                    gigAppsListForView.add(
                        GigAppListRecyclerItemData.GigAppListSearchRecyclerItemData(
                            "",
                            this
                        )
                    )
                }

                gigApps.forEach {
                    gigAppsListForView.add(
                        GigAppListRecyclerItemData.GigAppRecyclerItemData(
                            status = it.status.toString(),
                            jobProfileId = it.jobProfileId,
                            tradeName = it.tradeName.toString(),
                            profileName = it.profileName.toString(),
                            companyLogo = it.companyLogo.toString(),
                            selected = it.isSelected,
                            this
                        )
                    )
                }
            }
            //gigAppListShownOnView = gigAppsListForView
            _viewState.postValue(
                SelectGigAppViewState.GigAppListLoaded(
                    gigAppsListForView
                )
            )

            logger.d(
                TAG,
                "${gigAppListShownOnView.size} items (joinings + status) shown on view"
            )
        } catch (e: Exception) {

        }
    }

    fun searchOtherApplications(
        searchString: String
    ) {
        logger.d(TAG, "new search string received : '$searchString'")
        //this.currentSearchString = searchString
////
//        if (gigAppListShownOnView.isEmpty()) {
//            _viewState.postValue(SelectGigAppViewState.NoGigAppsFound)
//            return
//        }
        //processGigAppssAndEmit(ongoingGigApplications, otherGigApplications)
        //processGigApps(jobProfiles)

        if (searchString.isEmpty()) {
            jobProfilesShownOnView = jobProfiles
            logger.d(TAG, "Job profiles found empty search : ${jobProfiles.size}")
            processGigApps(jobProfilesShownOnView)
            return
        } else {
            jobProfilesShownOnView = jobProfiles.filter {
                it.tradeName?.contains(searchString, true) ?: false
                        || it.profileName?.contains(searchString, true) ?: false
            }
        }

        processGigApps(jobProfilesShownOnView)
        logger.d(TAG, "Job profiles found after search : ${jobProfilesShownOnView.size}")
    }

    fun selectJobProfile(
        jobProfile: GigAppListRecyclerItemData.GigAppRecyclerItemData
    ) {
        logger.d(TAG, "selecting job profile ${jobProfile.jobProfileId}...")

        if (currentlySelectedGigIndex == -1) {
            currentlySelectedGigIndex = jobProfilesShownOnView.indexOfFirst {
                it.jobProfileId == jobProfile.jobProfileId
            }
            logger.d(
                TAG,
                "no job profile selected yet, selecting index no $currentlySelectedGigIndex"
            )

            if (currentlySelectedGigIndex != -1) {
                _selectedIndex.value = currentlySelectedGigIndex
                jobProfilesShownOnView[currentlySelectedGigIndex].isSelected = true
                setSelectedJobProfile()
            }

        } else {

            val newSelectedItemIndex = jobProfilesShownOnView.indexOfFirst {
                it.jobProfileId == jobProfile.jobProfileId
            }

            if (newSelectedItemIndex == currentlySelectedGigIndex) {
                //Item Already selected
                return
            }

            jobProfilesShownOnView[currentlySelectedGigIndex].isSelected = false
            jobProfilesShownOnView[newSelectedItemIndex].isSelected = true

            logger.d(
                TAG,
                "already profile selected yet, selecting index no $newSelectedItemIndex, deselecting : $currentlySelectedGigIndex"
            )

            currentlySelectedGigIndex = newSelectedItemIndex
            _selectedIndex.value = newSelectedItemIndex
            setSelectedJobProfile()
        }

        processGigApps(jobProfilesShownOnView)
    }

    fun setSelectedJobProfile() {
        _selectedJobProfileOverview.value = jobProfilesShownOnView.get(currentlySelectedGigIndex)
    }

    fun getSelectedJobProfile(): JobProfileOverview {
        return jobProfilesShownOnView.get(currentlySelectedGigIndex)
    }

    fun getSelectedIndex(): Int {
        return currentlySelectedGigIndex
    }

    fun fetchInfoAndStartJoiningProcess(
        userUid: String,
        joiningId: String?,
        jobProfileOverview: JobProfileOverview
    ) = viewModelScope.launch {
        _viewState.value = SelectGigAppViewState.FetchingDataToStartJoiningProcess

        val profile = try {
            profileFirebaseRepository.getProfileOrThrow(userUid)
        } catch (e: Exception) {
            _viewState.value = SelectGigAppViewState.ErrorInStartingJoiningProcess(
                error = "Unable to start joining"
            )
            return@launch
        }

        var finalJoiningId = ""
        if (joiningId == null) {
            try {
                finalJoiningId = leadManagementRepo.createOrUpdateJoiningDocumentWithJoiningPending(
                    userUid = userUid,
                    jobProfileId = jobProfileOverview.jobProfileId,
                    name = profile.name,
                    jobProfileName = jobProfileOverview.profileName ?: "",
                    phoneNumber = profile.loginMobile,
                    tradeName = jobProfileOverview.tradeName ?: "",
                    lastStatusChangeSource = "SelectGigApplicationToActivateViewModel"
                )
            } catch (e: Exception) {
                _viewState.value = SelectGigAppViewState.ErrorInStartingJoiningProcess(
                    error = "Unable to start joining"
                )
                return@launch
            }
        } else {
            finalJoiningId = joiningId
        }


        try {
            val gigerProfileInfo = GigerProfileCardDVM(
                gigerImg = profile.getFullProfilePicPathThumbnail() ?: "",
                name = profile.name,
                number = profile.loginMobile,
                jobProfileName = jobProfileOverview.profileName ?: "",
                jobProfileLogo = jobProfileOverview.companyLogo ?: ""
            )

            val assignGigRequest = AssignGigRequest(
                joiningId = finalJoiningId,
                jobProfileId = jobProfileOverview.jobProfileId,
                jobProfileName = jobProfileOverview.profileName ?: "",
                userName = "",
                userUid = "",
                enrollingTlUid = "",
                assignGigsFrom = Timestamp.now(),
                cityId = "",
                cityName = "",
                location = JobLocation(
                    id = "",
                    type = "",
                    name = null
                ),
                shift = listOf(),
                gigForceTeamLeaders = listOf(),
                businessTeamLeaders = listOf()
            )

            _viewState.value = SelectGigAppViewState.StartGigerJoiningProcess(
                gigerInfo = gigerProfileInfo,
                assignGigRequest = assignGigRequest
            )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "unable to start joining process",
                e
            )

            _viewState.value = SelectGigAppViewState.ErrorInStartingJoiningProcess(
                error = "Unable to start joining process, please try again later"
            )
        }
    }

}

