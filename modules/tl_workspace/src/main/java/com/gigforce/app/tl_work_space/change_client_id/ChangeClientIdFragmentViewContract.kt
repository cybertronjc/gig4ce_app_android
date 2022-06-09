package com.gigforce.app.tl_work_space.change_client_id

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState


sealed class ChangeClientIdFragmentUiState : UiState {

    object ScreenInitializedOrRestored : ChangeClientIdFragmentUiState()

    object ChangingClientId : ChangeClientIdFragmentUiState()

    data class ErrorWhileChangingClientId(
        val error: String
    ) : ChangeClientIdFragmentUiState()

    object ClientIdChanged : ChangeClientIdFragmentUiState()
}

sealed class ChangeClientIdFragmentViewEvents : UiEvent {

    object ChangeClientIdClicked : ChangeClientIdFragmentViewEvents()

    data class NewClientIdEntered(
        val reason: String
    ) : ChangeClientIdFragmentViewEvents()
}

sealed class ChangeClientIdFragmentViewUiEffects : UiEffect {

    object EnableSubmitButton : ChangeClientIdFragmentViewUiEffects()

    object DisableSubmitButton : ChangeClientIdFragmentViewUiEffects()

    data class ClientIdValidationError(
        val error: String?
    ) : ChangeClientIdFragmentViewUiEffects()
}
