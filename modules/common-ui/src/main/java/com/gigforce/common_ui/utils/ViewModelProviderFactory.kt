package com.gigforce.common_ui.utils

import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelProviderFactory(private val viewModel: ViewModel?) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewModel!!.javaClass)) {
            return viewModel as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}