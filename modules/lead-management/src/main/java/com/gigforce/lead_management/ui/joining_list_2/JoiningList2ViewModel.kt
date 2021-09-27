package com.gigforce.lead_management.ui.joining_list_2

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningNew
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningSignUpInitiatedMode
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningStatus
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData
import com.gigforce.lead_management.repositories.LeadManagementRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject



@HiltViewModel
class JoiningList2ViewModel @Inject constructor(
    @ApplicationContext private val appContext : Context,
    private val leadManagementRepository: LeadManagementRepository,
    private val gigforceLogger: GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "JoiningListViewModel"
    }

    //State Observables
    private val _viewState = MutableLiveData<JoiningList2ViewState>()
    val viewState: LiveData<JoiningList2ViewState> = _viewState

    private val _filters = MutableLiveData<JoiningFilters>()
    val filters: LiveData<JoiningFilters> = _filters

    private val _filtersMap = MutableLiveData<Map<String, Int>>()
    val filterMap: LiveData<Map<String, Int>> = _filtersMap

    //Data
    private var joiningsRaw: List<JoiningNew> = emptyList()
    private var joiningListShownOnView: MutableList<JoiningList2RecyclerItemData> = mutableListOf()
    private var currentSearchString: String? = null
    var currentFilterString: String? = null
    private var fetchJoiningListener: ListenerRegistration? = null

    init {
        //startListeningToJoinings()
    }

    override fun onCleared() {
        super.onCleared()
        fetchJoiningListener?.remove()
        gigforceLogger.d(
            TAG,
            "removing listener to fetch joining query"
        )
    }

    fun getJoinings() = viewModelScope.launch {
        _viewState.postValue(JoiningList2ViewState.LoadingDataFromServer)
        try {
            gigforceLogger.d(TAG, "fetching job profiles...")

            joiningsRaw = leadManagementRepository.getJoiningListings()

            //_viewState.value = Lce.content(jobProfiles)
            processJoiningsAndEmit(joiningsRaw)

            gigforceLogger.d(TAG, "received ${joiningsRaw.size} joinings from server")

        } catch (e: Exception) {
            _viewState.value = JoiningList2ViewState.ErrorInLoadingDataFromServer(
                error = e.message ?: "Unable to fetch selections",
                shouldShowErrorButton = false
            )
            gigforceLogger.e(
                TAG,
                " getJoiningList()",
                e
            )
        }
    }

    private fun startListeningToJoinings() = viewModelScope.launch {
        _viewState.postValue(JoiningList2ViewState.LoadingDataFromServer)

        gigforceLogger.d(
            TAG,
            "listening to fetch joining query..."
        )
//        fetchJoiningListener = leadManagementRepository.fetchJoiningsQuery()
//            .addSnapshotListener { value, error ->
//
//                if (error != null) {
//                    gigforceLogger.e(
//                        TAG,
//                        "while listing to joining list",
//                        error
//                    )
//
//                    _viewState.postValue(
//                        JoiningList2ViewState.ErrorInLoadingDataFromServer(
//                            error = "Unable to fetch Joinings",
//                            shouldShowErrorButton = true
//                        )
//                    )
//                }
//
//                if (value != null) {
//                    gigforceLogger.d(
//                        TAG,
//                        " ${value.size()} joinings received from server"
//                    )
//
//                    joiningsRaw =value.documents.map {
//                        it.toObject(Joining::class.java)!!.apply {
//                            this.joiningId = it.id
//                        }
//                    }
//
//                    if (joiningsRaw.isEmpty()) {
//                        _viewState.postValue(JoiningList2ViewState.NoJoiningFound)
//                    } else {
//                        processJoiningsAndEmit(joiningsRaw)
//                        prepareFilters(joiningsRaw)
//                    }
//                }
//            }

        try {
            gigforceLogger.d(TAG, "fetching job profiles...")

             joiningsRaw = leadManagementRepository.getJoiningListings()

            //_viewState.value = Lce.content(jobProfiles)
            processJoiningsAndEmit(joiningsRaw)

            gigforceLogger.d(TAG, "received ${joiningsRaw.size} joinings from server")

        } catch (e: Exception) {
            _viewState.value = JoiningList2ViewState.NoJoiningFound
            gigforceLogger.e(
                TAG,
                " getJoiningList()",
                e
            )
        }
    }

    private fun processJoiningsAndEmit(
        joiningsRaw: List<JoiningNew>
    ) {

        val businessToJoiningGroupedList = joiningsRaw.filter {
            if (currentSearchString.isNullOrBlank())
                true
            else {
                it.gigerName?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
                        || it.gigerMobileNo?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
            }
        }.filter {
            if (currentFilterString.isNullOrBlank())
                true
            else {
                it.status.contains(
                    currentFilterString!!, true
                )
            }
        }.groupBy {
            it.business?.name
        }.toSortedMap(compareBy { it })


        val filterMap = HashMap<String, Int>()
        var totalCount = 0
        val joiningListForView = mutableListOf<JoiningList2RecyclerItemData>()
        businessToJoiningGroupedList.forEach { (business, joinings) ->
            gigforceLogger.d(TAG, "processing data, Status : $business : ${joinings.size} Joinings")


            joiningListForView.add(
                JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData(
                    business.toString()
                )
            )

            joinings.forEach {
                joiningListForView.add(
                    JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData(
                        _id = it._id,
                        assignGigsFrom = it.assignGigsFrom ?: "",
                        gigerName = it.gigerName ?: "",
                        gigerMobileNo = it.gigerMobileNo ?: "",
                        gigerId = it.gigerId,
                        profilePicture = it.profilePicture,
                        bussiness = it.business!!,
                        status = it.status,
                        selected = false,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }


        }



        joiningListShownOnView = joiningListForView
        if (joiningListShownOnView.isEmpty()) {

            _viewState.postValue(
                JoiningList2ViewState.NoJoiningFound
            )
        } else {
            _viewState.postValue(
                JoiningList2ViewState.JoiningListLoaded(
                    joiningList = joiningListShownOnView
                )
            )
        }

        //for filter count
        val statusToJoiningGroupedList = joiningsRaw.filter {
            if (currentSearchString.isNullOrBlank())
                true
            else {
                it.gigerName?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
                        || it.gigerMobileNo?.contains(
                    currentSearchString!!,
                    true
                ) ?: false
            }
        }.groupBy { it.status }.toSortedMap(compareBy { it })

        statusToJoiningGroupedList.forEach {
            totalCount += it.value.size
        }
        if (statusToJoiningGroupedList.containsKey("Pending")){
            filterMap.put(LeadManagementConstants.STATUS_PENDING, statusToJoiningGroupedList.get("Pending")?.size!!)
        } else {
            filterMap.put(LeadManagementConstants.STATUS_PENDING, 0)
        }
        if (statusToJoiningGroupedList.containsKey("Completed")){
            filterMap.put(LeadManagementConstants.STATUS_COMPLETED, statusToJoiningGroupedList.get("Completed")?.size!!)
        } else  {
            filterMap.put(LeadManagementConstants.STATUS_COMPLETED, 0)
        }

        filterMap.put("All", totalCount)
        _filtersMap.postValue(filterMap)

        gigforceLogger.d(
            TAG,
            "${joiningListShownOnView.size} items (joinings + status) shown on view"
        )
    }

    fun prepareFilters(joininData: List<Joining>){
        val joiningDa =  joininData ?: return
        val statuses = joiningDa
            .filter {
                !it.status.isNullOrBlank()
            }.distinctBy {
                it.status
            }.map { joiningItem ->
                JoiningStatusAndCountItemData(
                    status = joiningItem.status,
                    attendanceCount = joiningDa.count { joiningItem.status == it.status },
                    statusSelected = false
                )
            }.toMutableList()
            .apply {
                this.add(
                    0, JoiningStatusAndCountItemData(
                        status = "All",
                        attendanceCount = joiningDa.size,
                        statusSelected = true
                    )
                )
            }

        _filters.postValue(
            JoiningFilters(
                shouldRemoveOlderStatusTabs = true,
                attendanceStatuses = statuses
            )
        )
    }

    private fun getJoiningText(
        it: Joining
    ): String {
        return when (it.getStatus()) {
            JoiningStatus.SIGN_UP_PENDING -> {
                if (JoiningSignUpInitiatedMode.BY_LINK == it.signUpMode) {
                    "App invite sent ${getDateDifferenceFormatted(it.updatedOn)}"
                } else {
                    "Signup started ${getDateDifferenceFormatted(it.updatedOn)}"
                }
            }
            JoiningStatus.APPLICATION_PENDING -> {
                if (it.jobProfileNameInvitedFor.isNullOrBlank()) {
                    "No Application Link shared yet"
                } else {
                    "${it.jobProfileNameInvitedFor} invite sent ${getDateDifferenceFormatted(it.updatedOn)}"
                }
            }
            JoiningStatus.JOINING_PENDING -> {
                "Joining initiated ${getDateDifferenceFormatted(it.updatedOn)}"
            }
            JoiningStatus.JOINED -> {
                "Joined ${getDateDifferenceFormatted(it.updatedOn)}"
            }
            JoiningStatus.PENDING -> {
                "Pending ${getDateDifferenceFormatted(it.updatedOn)}"
            }
            JoiningStatus.COMPLETED -> {
                "Completed ${getDateDifferenceFormatted(it.updatedOn)}"
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
            _viewState.postValue(JoiningList2ViewState.NoJoiningFound)
            return
        }
        processJoiningsAndEmit(joiningsRaw)
    }

    fun filterJoinings(
        filterString: String
    ) {
        gigforceLogger.d(TAG, "new filter string received : '$filterString'")
        this.currentFilterString = filterString

        if (joiningsRaw.isEmpty()) {
            _viewState.postValue(JoiningList2ViewState.NoJoiningFound)
            return
        }
        processJoiningsAndEmit(joiningsRaw)
    }
}