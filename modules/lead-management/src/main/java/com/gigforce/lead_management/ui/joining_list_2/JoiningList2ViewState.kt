package com.gigforce.lead_management.ui.joining_list_2

import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData

sealed class JoiningList2ViewState {

    object LoadingDataFromServer : JoiningList2ViewState()

    object NoJoiningFound : JoiningList2ViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : JoiningList2ViewState()

    data class JoiningListLoaded(
        val joiningList: List<JoiningList2RecyclerItemData>
    ) : JoiningList2ViewState()
}

data class JoiningFilters(
    val shouldRemoveOlderStatusTabs: Boolean,
    val attendanceStatuses: List<JoiningStatusAndCountItemData>?
)