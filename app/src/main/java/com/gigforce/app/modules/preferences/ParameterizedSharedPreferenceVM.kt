package com.gigforce.app.modules.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gigforce.common_ui.configrepository.ConfigDataModel


class ParameterizedSharedPreferenceVM(val configDataModel: ConfigDataModel?) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SharedPreferenceViewModel(configDataModel) as T
    }
}