package com.gigforce.lead_management.ui.joining_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningSignUpInitiatedMode
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningStatus
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

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

@HiltViewModel
class JoiningListViewModel @Inject constructor(
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
    private var currentSearchString: String? = null
    private var fetchJoiningListener: ListenerRegistration? = null

    init {
        startListeningToJoinings()
    }

    override fun onCleared() {
        super.onCleared()
        fetchJoiningListener?.remove()
    }

    private fun startListeningToJoinings() = viewModelScope.launch {
        _viewState.postValue(JoiningListViewState.LoadingDataFromServer)

        gigforceLogger.d(
            TAG,
            "listening to fetch joining query..."
        )
        fetchJoiningListener = leadManagementRepository.fetchJoiningsQuery()
            .addSnapshotListener { value, error ->

                if (error != null) {
                    gigforceLogger.e(
                        TAG,
                        "while listing to joining list",
                        error
                    )

                    _viewState.postValue(
                        JoiningListViewState.ErrorInLoadingDataFromServer(
                            error = "Unable to fetch Joinings",
                            shouldShowErrorButton = true
                        )
                    )
                }

                if (value != null) {
                    gigforceLogger.d(
                        TAG,
                        " ${value.size()} joinings received from server"
                    )

                    joiningsRaw = value.toObjects(Joining::class.java)
                    if (joiningsRaw.isEmpty()) {
                        _viewState.postValue(JoiningListViewState.NoJoiningFound)
                    } else {
                        processJoiningsAndEmit(joiningsRaw)
                    }
                }
            }
    }

    private fun processJoiningsAndEmit(
        joiningsRaw: List<Joining>
    ) {

        val statusToJoiningGroupedList = joiningsRaw.filter {
            if (currentSearchString.isNullOrBlank())
                true
            else {
                it.name?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
                        || it.phoneNumber?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
            }
        }.groupBy {
            it.getStatus().getOverallStatusString()
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
                        userUid = it.uid,
                        userName = it.name ?: "N/A",
                        userProfilePicture = it.profilePicture ?: "",
                        userProfilePictureThumbnail = it.profilePicture ?: "",
                        userProfilePhoneNumber = it.phoneNumber ?: "",
                        status = it.getStatus().getStatusString(),
                        joiningStatusText = getJoiningText(it)
                    )
                )
            }
        }

        joiningListShownOnView = joiningListForView
        if (joiningListShownOnView.isEmpty()) {

            _viewState.postValue(
                JoiningListViewState.NoJoiningFound
            )
        } else {
            _viewState.postValue(
                JoiningListViewState.JoiningListLoaded(
                    joiningList = joiningListShownOnView
                )
            )
        }

        gigforceLogger.d(
            TAG,
            "${joiningListShownOnView.size} items (joinings + status) shown on view"
        )
    }

    private fun getJoiningText(
        it: Joining
    ): String {
        return when (it.getStatus()) {
            JoiningStatus.SIGN_UP_PENDING -> {
                if (JoiningSignUpInitiatedMode.BY_LINK == it.signUpMode) {
                    "Onboarding started ${getDateDifferenceFormatted(it.updatedOn)}"
                } else if (JoiningSignUpInitiatedMode.BY_LINK == it.signUpMode) {
                    "App invite sent ${getDateDifferenceFormatted(it.updatedOn)}"
                } else {
                    "Signup started ${getDateDifferenceFormatted(it.updatedOn)}"
                }
            }
            JoiningStatus.APPLICATION_PENDING -> {
                if (it.applicationNameInvitedFor != null) {
                    "No Application Link shared yet"
                } else if(it.applicationNameInvitedFor != null){
                    "${it.applicationNameInvitedFor} invite sent ${getDateDifferenceFormatted(it.updatedOn)}"
                } else {
                    "Application Pending"
                }
            }
            JoiningStatus.JOINING_PENDING -> {
                "Joining initiated ${getDateDifferenceFormatted(it.updatedOn)}"
            }
            JoiningStatus.JOINED -> {
                "Joined ${getDateDifferenceFormatted(it.updatedOn)}"
            }
        }
    }

    private fun getDateDifferenceFormatted(updatedOn: Timestamp): String {
        val updateOnDate = updatedOn.toLocalDate()
        val currentDate = LocalDate.now()

        return if (currentDate.isEqual(updateOnDate)) {
            "today"
        } else {
            val daysDiff = Duration.between(
                updateOnDate.atStartOfDay(),
                currentDate.atStartOfDay()
            ).toDays()

            "$daysDiff day(s) ago"
        }
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