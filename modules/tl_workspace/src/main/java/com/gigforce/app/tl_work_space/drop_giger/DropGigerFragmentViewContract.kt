package com.gigforce.app.tl_work_space.drop_giger

import com.gigforce.app.android_common_utils.base.viewModel.UiEffect
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.android_common_utils.base.viewModel.UiState
import com.gigforce.app.tl_work_space.drop_giger.models.DropOption
import java.time.LocalDate
import kotlin.random.Random


sealed class DropGigerFragmentUiState : UiState {

    object LoadingDropOptionsData : DropGigerFragmentUiState()

    data class ErrorWhileLoadingDropOptions(
        val error: String
    ) : DropGigerFragmentUiState()

    data class ShowOptionsData(
        val options: List<DropOption>
    ) : DropGigerFragmentUiState() {

        override fun equals(other: Any?): Boolean {
            return false
        }

        override fun hashCode(): Int {
            return Random.nextInt()
        }
    }

    object DroppingGiger : DropGigerFragmentUiState(){

        override fun equals(other: Any?): Boolean {
            return false
        }

        override fun hashCode(): Int {
            return Random.nextInt()
        }
    }

    data class ErrorWhileDroppingGiger(
        val error: String
    ) : DropGigerFragmentUiState(){

        override fun equals(other: Any?): Boolean {
            return false
        }

        override fun hashCode(): Int {
            return Random.nextInt()
        }
    }

    object GigerDroppedWithSuccess : DropGigerFragmentUiState(){

        override fun equals(other: Any?): Boolean {
            return false
        }

        override fun hashCode(): Int {
            return Random.nextInt()
        }
    }


    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }
}

sealed class DropGigerFragmentViewEvents : UiEvent {

    object DropButtonClicked : DropGigerFragmentViewEvents()

    data class LastWorkingDateSelected(
        val date: LocalDate
    ) : DropGigerFragmentViewEvents()

    data class ReasonSelected(
        val reason: DropOption
    ) : DropGigerFragmentViewEvents()

    data class CustomReasonEntered(
        val reason: String
    ) : DropGigerFragmentViewEvents()
}

sealed class DropGigerFragmentViewUiEffects : UiEffect {

    object ShowCustomReasonLayout : DropGigerFragmentViewUiEffects()

    object HideCustomReasonLayout : DropGigerFragmentViewUiEffects()

    object EnableSubmitButton : DropGigerFragmentViewUiEffects()

    object DisableSubmitButton : DropGigerFragmentViewUiEffects()
}
