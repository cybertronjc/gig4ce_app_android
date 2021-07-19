package com.gigforce.lead_management.ui.joining_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import kotlinx.coroutines.launch

sealed class JoiningListViewState {

    object LoadingDataFromServer : JoiningListViewState()

    object NoJoiningFound : JoiningListViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : JoiningListViewState()

    data class JoiningListLoaded(
        val joiningList: List<JoiningListRecyclerItemData>
    ) : JoiningListViewState()
}

class JoiningListViewModel constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val gigforceLogger: GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "JoiningListViewModel"
    }

    private val _viewState = MutableLiveData<JoiningListViewState>()
    val viewState: LiveData<JoiningListViewState> = _viewState

    //Data
    private var joiningsRaw: List<Joining> = emptyList()
    private var joiningListShownOnView: MutableList<JoiningListRecyclerItemData> = mutableListOf()
    private var currentSearchString : String? = null

    init {
        refreshJoinings()
    }

    fun refreshJoinings() = viewModelScope.launch {
        _viewState.postValue(JoiningListViewState.LoadingDataFromServer)
        gigforceLogger.d(
            TAG,
            "fetching joining list..."
        )

        try {
            joiningsRaw = leadManagementRepository.fetchJoinings()
            gigforceLogger.d(
                TAG,
                " ${joiningsRaw.size} joinings received from server"
            )

            processJoiningsAndEmit(joiningsRaw)
        } catch (e: Exception) {
            gigforceLogger.e(
                TAG,
                "while fetching joining list",
                e
            )

            _viewState.postValue(
                JoiningListViewState.ErrorInLoadingDataFromServer(
                    error = e.message ?: "Unable to fetch joinings",
                    shouldShowErrorButton = true
                )
            )
        }
    }

    private fun processJoiningsAndEmit(
        joiningsRaw: List<Joining>
    ) {

        val statusToJoiningGroupedList = joiningsRaw.filter {
            it.status != null
        }.filter {
            if(currentSearchString.isNullOrBlank())
                true
            else
               it.status?.contains(
                   currentSearchString!!,
                   true
               ) ?: false
        }.groupBy {
            it.status!!
        }

        val joiningListForView = mutableListOf<JoiningListRecyclerItemData>()
        statusToJoiningGroupedList.forEach { (status, joinings) ->
            gigforceLogger.d(TAG, "processing data, Status : $status : ${joinings.size} Joinings")

            joiningListForView.add(
                JoiningListRecyclerItemData.JoiningListRecyclerStatusItemData(
                    "$status (${joinings.size})"
                )
            )

            joinings.forEach {
                joiningListForView.add(
                    JoiningListRecyclerItemData.JoiningListRecyclerJoiningItemData(
                        userUid = it.uid ?: "",
                        userName = it.name ?: "N/A",
                        userProfilePicture = it.profilePicture ?: "",
                        userProfilePictureThumbnail = it.profilePicture ?: "",
                        userProfilePhoneNumber = it.phoneNumber ?: "",
                        status = it.status ?: "",
                        joiningStatusText = it.joiningStatusText ?: ""
                    )
                )
            }
        }

        joiningListShownOnView = joiningListForView
        _viewState.postValue(
            JoiningListViewState.JoiningListLoaded(
                joiningList = joiningListShownOnView
            )
        )

        gigforceLogger.d(TAG, "${joiningListShownOnView.size} items (joinings + status) shown on view")
    }

    fun searchJoinings(
        searchString: String
    ) {
        gigforceLogger.d(TAG, "new search string received : '$searchString'")
        this.currentSearchString = searchString

        if (joiningsRaw.isEmpty()) {
            _viewState.postValue(JoiningListViewState.NoJoiningFound)
            return
        }
        processJoiningsAndEmit(joiningsRaw)
    }
}