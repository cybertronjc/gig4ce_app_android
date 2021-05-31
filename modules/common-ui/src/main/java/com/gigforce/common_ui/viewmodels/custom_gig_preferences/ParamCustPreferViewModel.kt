package com.gigforce.common_ui.viewmodels.custom_gig_preferences

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ParamCustPreferViewModel(val lifecycleOwner: LifecycleOwner) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomPreferencesViewModel(lifecycleOwner) as T
    }
}