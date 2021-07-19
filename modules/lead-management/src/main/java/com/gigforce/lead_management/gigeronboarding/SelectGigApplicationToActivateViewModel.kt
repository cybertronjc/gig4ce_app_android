package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigApplication
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigForGigerActivation
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.LeadManagementRepo
import com.gigforce.lead_management.gigeronboarding.views.GigAppListRecyclerItemView
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.ui.joining_list.JoiningListViewModel
import com.gigforce.lead_management.ui.joining_list.JoiningListViewState
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

sealed class SelectGigAppViewState {

    object LoadingDataFromServer : SelectGigAppViewState()

    object NoGigAppsFound : SelectGigAppViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : SelectGigAppViewState()

    data class GigAppListLoaded(
        val gigAppList: List<GigAppListRecyclerItemData>
    ) : SelectGigAppViewState()
}

class SelectGigApplicationToActivateViewModel(
    private val leadManagementRepo: LeadManagementRepo,
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

//        val statusToGigAppList1 = gigAppList.filter {
//            it.status != null
//        }.filter {
//            if(currentSearchString.isNullOrBlank())
//                true
//            else
//                it.status?.contains(
//                    currentSearchString!!,
//                    true
//                ) ?: false
//        }.groupBy {
//            it.status!!
//        }

        val gigAppList: List<GigApplication> = ongoingGigApps + otherGigApps
        val statusToGigAppList: List<GigApplication> =
            ongoingGigApps.orEmpty() + otherGigApps.orEmpty()
//        statusToGigAppList.groupBy {
//            if (it.status?.isEmpty()!!) "OtherApplications" else "OnGoing Applications"
//        }

        val gigAppsListForView = mutableListOf<GigAppListRecyclerItemData>()
        statusToGigAppList.forEachIndexed { index, gigApplication ->
            gigforceLogger.d(TAG, "processing data, Status :  : ${statusToGigAppList.size} GigApps")

            gigAppsListForView.add(
                GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData(
                    if (gigApplication.status?.isEmpty()!!) "Other Applications" else "OnGoing Applications"
                )
            )

            if (gigApplication.status?.isEmpty()!!) {
                gigAppsListForView.add(
                    GigAppListRecyclerItemData.GigAppListSearchRecyclerItemData(
                        "Search"
                    )
                )
            }
            gigAppList.forEach {
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

        gigAppListShownOnView = gigAppsListForView
        _viewState.postValue(
            SelectGigAppViewState.GigAppListLoaded(
                gigAppListShownOnView
            )
        )

        gigforceLogger.d(
            TAG,
            "${gigAppListShownOnView.size} items (joinings + status) shown on view"
        )
    }

    fun searchJoinings(
        searchString: String
    ) {
        gigforceLogger.d(TAG, "new search string received : '$searchString'")
        this.currentSearchString = searchString

        if (gigAppListShownOnView.isEmpty()) {
            _viewState.postValue(SelectGigAppViewState.NoGigAppsFound)
            return
        }
        processGigAppssAndEmit(ongoingGigApplications, otherGigApplications)
    }
}

