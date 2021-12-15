package com.gigforce.modules.feature_chat.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.remote.verification.KycOcrResultModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatSettingsViewModel@Inject constructor(): ViewModel() {

    val _autoDownload = MutableLiveData<Boolean>()
    val autoDownload: LiveData<Boolean> = _autoDownload

}