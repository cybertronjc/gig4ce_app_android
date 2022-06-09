package com.gigforce.app.tl_work_space

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import kotlinx.coroutines.launch

abstract class BaseTLWorkSpaceViewModel<
        Event : UiEvent,
        State : UiState,
        Effect : UiEffect>(initialState: State) : BaseViewModel<
        Event,
        State,
        Effect>(initialState = initialState) {

    protected lateinit var sharedViewModel: TLWorkSpaceSharedViewModel

    open fun setSharedViewModel(
        sharedViewModel: TLWorkSpaceSharedViewModel
    ) = viewModelScope.launch {

        this@BaseTLWorkSpaceViewModel.sharedViewModel = sharedViewModel
        sharedViewModel
            .sharedEvent
            .collect {
                consumeSharedEvents(it)
            }
    }

    private fun consumeSharedEvents(it: TLWorkSpaceSharedViewModelEvent) {
        when (it) {
            is TLWorkSpaceSharedViewModelEvent.ClientIdUpdatedOfGiger -> clientIdChanged(
                it.newClientId
            )
            is TLWorkSpaceSharedViewModelEvent.GigerDropped -> gigerDropped(
                gigerId = it.gigerId,
                jobProfileId = it.jobProfileId
            )
            is TLWorkSpaceSharedViewModelEvent.TeamLeaderChanged -> teamLeaderChangedOf(
                gigerId = it.gigerId,
                jobProfileId = it.jobProfileId
            )
        }
    }

    open fun clientIdChanged(
        newClientId: String
    ) {
    }

    open fun gigerDropped(
        gigerId: String,
        jobProfileId: String
    ) {
    }

    open fun teamLeaderChangedOf(
        gigerId: String,
        jobProfileId: String
    ) {
    }
}