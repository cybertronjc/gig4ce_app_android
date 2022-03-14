package com.gigforce.lead_management.ui.joining_list_2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningNew
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


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
    private var joiningsRaw: List<JoiningNew>? = null
    private var joiningListShownOnView: MutableList<JoiningList2RecyclerItemData> = mutableListOf()
    private var currentSearchString: String? = null
    var currentFilterString: String = "Pending"
    var filterDaysVM: Int? = null

    private var fetchJoiningListener: ListenerRegistration? = null
    var isSelectEnableGlobal = false

    var dropBusinessMap : HashMap<String, Int>? = HashMap()
    var dropJoining : HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>? = HashMap()

    private val _dropJoiningMap = MutableLiveData<HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>>()
    val dropJoiningMap: LiveData<HashMap<JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData, Boolean>> = _dropJoiningMap

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

            val joiningsReceived = leadManagementRepository.getJoiningListings()
            joiningsRaw = joiningsReceived

            //_viewState.value = Lce.content(jobProfiles)
            processJoiningsAndEmit(joiningsReceived)
            gigforceLogger.d(TAG, "received ${joiningsReceived.size} joinings from server")

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
                if (currentFilterString == "Dropped"){
                    it.isActive == false
                }else{
                    it.status.contains(
                        currentFilterString!!, true
                    ) && it.isActive == true

                }

            }
        }.filter {
            if (filterDaysVM == null || filterDaysVM == -1)
                true
            else if (filterDaysVM in 0..1){
                getDateDifference(it.createdAt.toString()) == filterDaysVM!!
            }
            else {
                getDateDifference(it.createdAt.toString()) <= filterDaysVM!!
            }
        }.groupBy {
            it.business?.name
        }.toSortedMap(compareBy { it })


        val filterMap = HashMap<String, Int>()
        var droppedCount = 0
        var pendingCount = 0
        var completedCount = 0
        val joiningListForView = mutableListOf<JoiningList2RecyclerItemData>()
        businessToJoiningGroupedList.forEach { (business, joinings) ->
            gigforceLogger.d(TAG, "processing data, Status : $business : ${joinings.size} Joinings")
            var isVisible = true

            if (dropBusinessMap?.containsKey(business) == true){
                isVisible = dropBusinessMap!!.get(business) == 0
            }

            joiningListForView.add(
                JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData(
                    business.toString() + "(${joinings.size})",
                    isVisible
                )
            )

            var isSelectEnable = false
            if (dropJoining?.isNotEmpty() == true){
                isSelectEnable = true
            }else if (isSelectEnableGlobal){
                isSelectEnable = true
            }

            joinings.forEach {
                var isSelected = false

                val currentObjectMatch = dropJoining?.keys?.find { key -> key._id == it._id }
                if (currentObjectMatch != null){
                    isSelected = dropJoining?.get(currentObjectMatch)!!
                }
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
                        selected = isSelected,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        isVisible = isVisible,
                        isActive = it.isActive!!,
                        isSelectEnable,
                        this
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
        }.filter {
            if (filterDaysVM == null || filterDaysVM == -1)
                true
            else if (filterDaysVM in 0..1){
                getDateDifference(it.createdAt.toString()) == filterDaysVM!!
            }
            else {
                getDateDifference(it.createdAt.toString()) <= filterDaysVM!!
            }
        }.groupBy { it.status }.toSortedMap(compareBy { it })


        statusToJoiningGroupedList.forEach {
            //totalCount += it.value.size
            it.value.forEach {
                if (it.isActive == false){
                    droppedCount ++
                }else {
                    if (it.status == "Pending"){
                        pendingCount ++
                    }else if (it.status == "Completed"){
                        completedCount ++
                    }
                }
            }
        }
//        val activeJoinings = statusToJoiningGroupedList.filter { it.key == true }.values
//        activeJoinings.forEach {
//            if (it.all { it.status == "Pending" }){
//                filterMap.put(LeadManagementConstants.STATUS_PENDING, it.all { it.status == "Pending"})
//            }
//        }

//        if (statusToJoiningGroupedList.containsKey("Pending")){
//            filterMap.put(LeadManagementConstants.STATUS_PENDING, statusToJoiningGroupedList.get("Pending")?.size!!)
//        } else {
//            filterMap.put(LeadManagementConstants.STATUS_PENDING, 0)
//        }
//        if (statusToJoiningGroupedList.containsKey("Completed")){
//            filterMap.put(LeadManagementConstants.STATUS_COMPLETED, statusToJoiningGroupedList.get("Completed")?.size!!)
//        } else  {
//            filterMap.put(LeadManagementConstants.STATUS_COMPLETED, 0)
//        }

        filterMap.put("Dropped", droppedCount)
        filterMap.put("Pending", pendingCount)
        filterMap.put("Completed", completedCount)
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
                        status = "Dropped",
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

    private fun getDateDifference(createdAt: String): Int {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val date = LocalDate.parse(createdAt, formatter)
        return if (currentDate.isEqual(date)){
            0
        } else {
            val daysDiff = Duration.between(
                date.atStartOfDay(),
                currentDate.atStartOfDay()
            ).toDays()
            daysDiff.toInt()
        }
    }

    fun searchJoinings(
        searchString: String
    ) {
        gigforceLogger.d(TAG, "new search string received : '$searchString'")
        this.currentSearchString = searchString

        if(joiningsRaw == null){
            return
        }

        if (joiningsRaw!!.isEmpty()) {
            _viewState.postValue(JoiningList2ViewState.NoJoiningFound)
            return
        }
        processJoiningsAndEmit(joiningsRaw!!)
    }

    fun filterJoinings(
        filterString: String
    ) {
        gigforceLogger.d(TAG, "new filter string received : '$filterString'")
        this.currentFilterString = filterString

        if(joiningsRaw == null){
            return
        }

        if (joiningsRaw!!.isEmpty()) {
            _viewState.postValue(JoiningList2ViewState.NoJoiningFound)
            return
        }
        processJoiningsAndEmit(joiningsRaw!!)
    }

    fun clickDropdown(businessName: String, dropEnabled: Boolean){
        gigforceLogger.d(TAG, "new dropdown click received $businessName , $dropEnabled")
        if (dropEnabled){
            dropBusinessMap?.put(businessName, 0)
        } else {
            dropBusinessMap?.put(businessName, 1)
        }
        processJoiningsAndEmit(joiningsRaw!!)

    }

    fun filterDaysJoinings(
        filterDays: Int
    ){
        gigforceLogger.d(TAG, "new filter click received $filterDays")
        filterDaysVM = filterDays

        if(joiningsRaw != null)
        processJoiningsAndEmit(joiningsRaw!!)

    }

    fun dropSelection(
        joiningInfo: JoiningList2RecyclerItemData.JoiningListRecyclerJoiningItemData,
        dropSelected: Boolean
    ){
        gigforceLogger.d(TAG, "new drop selection ${joiningInfo._id}, $dropSelected")
        if (dropSelected){
            dropJoining?.put(joiningInfo, true)
        } else {
            if (dropJoining?.containsKey(joiningInfo) == true){
                dropJoining?.remove(joiningInfo)
            }
        }
        isSelectEnableGlobal = true
        _dropJoiningMap.postValue(dropJoining)
        processJoiningsAndEmit(joiningsRaw!!)
    }

    fun resetViewModel(){
        gigforceLogger.d(TAG, "reset viewmodel")
        dropJoining?.clear()
        dropBusinessMap?.clear()
        isSelectEnableGlobal = false
        dropBusinessMap?.clear()
        //processJoiningsAndEmit(joiningsRaw!!)
    }

    fun getSelectEnableGlobal(): Boolean {
        return isSelectEnableGlobal
    }

    fun clearCachedRawJoinings(){
        joiningsRaw = null
    }
}