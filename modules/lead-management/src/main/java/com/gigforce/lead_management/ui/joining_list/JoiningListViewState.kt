package com.gigforce.lead_management.ui.joining_list

import com.gigforce.lead_management.models.JoiningListRecyclerItemData

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