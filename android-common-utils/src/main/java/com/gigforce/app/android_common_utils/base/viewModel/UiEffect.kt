package com.gigforce.app.android_common_utils.base.viewModel

/**
 * [UiEffect] represent one time operation, unlike [UiState] [UiEffect] is not restored
 * on getting back to the previous screen, or restoring state
 * example : Showing a snackbar, navigation events
 */
interface UiEffect