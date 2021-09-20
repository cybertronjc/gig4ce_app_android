package com.gigforce.lead_management.ui.joining_list_2

import com.gigforce.lead_management.models.JoiningListRecyclerItemData

sealed class JoiningList2ViewState {

    object LoadingDataFromServer : JoiningList2ViewState()

    object NoJoiningFound : JoiningList2ViewState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : JoiningList2ViewState()

    data class JoiningListLoaded(
        val joiningList: List<JoiningListRecyclerItemData>
    ) : JoiningList2ViewState()
}