package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigApplication
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.gigforce.lead_management.ui.share_application_link.ShareApplicationLinkViewModel
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
}

@HiltViewModel
class SelectGigApplicationToActivateViewModel @Inject constructor(
    private val leadManagementRepo: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener

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

     fun getJobProfilesToActivate(userUid: String) = viewModelScope.launch {
        _viewState.postValue(SelectGigAppViewState.LoadingDataFromServer)

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
            _viewState.value =   SelectGigAppViewState.ErrorInLoadingDataFromServer(
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

    private fun processGigApps(jobApps: List<JobProfileOverview>){
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
            if (it.ongoing == false) "Ongoing Applications" else "Other Applications"
        }

        gigAppsListForView.clear()
        try {
            if (jobApps.isEmpty()){
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
            } else if (jobApps.isEmpty()){
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
        }catch (e: Exception){

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
        }
        else {
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
            logger.d(TAG,
                "no job profile selected yet, selecting index no $currentlySelectedGigIndex"
            )

            if (currentlySelectedGigIndex != -1) {
                jobProfilesShownOnView[currentlySelectedGigIndex].isSelected = true
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

            logger.d(TAG,
                "already profile selected yet, selecting index no $newSelectedItemIndex, deselecting : $currentlySelectedGigIndex"
            )

            currentlySelectedGigIndex = newSelectedItemIndex
        }

        processGigApps(jobProfilesShownOnView)
    }

    fun getSelectedJobProfile(): JobProfileOverview{
        return jobProfilesShownOnView.get(currentlySelectedGigIndex)
    }
}

