package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigApplication
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigForGigerActivation
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
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
    private val gigforceLogger: GigforceLogger

) : ViewModel() {

    companion object {
        private const val TAG = "SelectGigApplicationViewModel"
    }

    private val _viewState = MutableLiveData<SelectGigAppViewState>()
    val viewState: LiveData<SelectGigAppViewState> = _viewState

    //Data
    private var gigAppList: List<GigForGigerActivation> = emptyList()
    private var gigAppListShownOnView: MutableList<GigAppListRecyclerItemData> = mutableListOf()
    private var currentSearchString: String? = null

    private var otherGigApplications: List<GigApplication> = emptyList()
    private var ongoingGigApplications: List<GigApplication> = emptyList()


    fun fetchGigApplications(gigerId: String) = viewModelScope.launch {
        _viewState.postValue(SelectGigAppViewState.LoadingDataFromServer)
        gigforceLogger.d(
            TAG,
            "fetching gig application list..."
        )

        try {
            otherGigApplications = leadManagementRepo.getOtherApplications()
            ongoingGigApplications = leadManagementRepo.getJobProfiles(gigerId)
            gigforceLogger.d(
                TAG,
                " ${ongoingGigApplications.size} ongoing applications received from server"
            )
            gigforceLogger.d(
                TAG,
                " ${otherGigApplications.size} other application received from server"
            )

            processGigAppssAndEmit(ongoingGigApplications, otherGigApplications)
        } catch (e: Exception) {
            gigforceLogger.e(
                TAG,
                "while fetching Gig App list",
                e
            )

            _viewState.postValue(
                SelectGigAppViewState.ErrorInLoadingDataFromServer(
                    error = e.message ?: "Unable to fetch gigApps",
                    shouldShowErrorButton = true
                )
            )
        }

    }

    private fun processGigAppssAndEmit(
        ongoingGigApps: List<GigApplication>,
        otherGigApps: List<GigApplication>
    ) {
        val gigAppList: List<GigApplication> = ongoingGigApps + otherGigApps
        val statusToGigAppList = gigAppList.filter {
            it.type != null
        }.groupBy {
            it.type
        }

        val gigAppsListForView = mutableListOf<GigAppListRecyclerItemData>()
        try {
            if (otherGigApps.isEmpty()){
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
            } else if (ongoingGigApps.isEmpty()){
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

            statusToGigAppList.forEach { (type, gigApps) ->
           gigforceLogger.d(TAG, "processing data, Status :  : ${statusToGigAppList.size} GigApps")

           gigAppsListForView.add(
               GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData(
                   "$type"
               )
           )

           if (type.equals("Other Applications")) {
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
                       userUid = it.gigerId.toString(),
                       status = it.status.toString(),
                       businessName = it.profileName.toString(),
                       jobProfileTitle = it.jobProfileTitle.toString(),
                       businessLogo = it.image.toString(),
                       businessLogoThumbnail = it.image.toString()
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

        gigforceLogger.d(
            TAG,
            "${gigAppListShownOnView.size} items (joinings + status) shown on view"
        )
        }catch (e: Exception){

        }
    }

    fun searchOtherApplications(
        searchString: String
    ) {
        gigforceLogger.d(TAG, "new search string received : '$searchString'")
        this.currentSearchString = searchString
////
//        if (gigAppListShownOnView.isEmpty()) {
//            _viewState.postValue(SelectGigAppViewState.NoGigAppsFound)
//            return
//        }
        processGigAppssAndEmit(ongoingGigApplications, otherGigApplications)
    }
}

