package com.gigforce.app.modules.preferences

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.utils.configrepository.ConfigDataModel


class ParameterizedSharedPreferenceVM(val configDataModel: ConfigDataModel?) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SharedPreferenceViewModel(configDataModel) as T
    }
}