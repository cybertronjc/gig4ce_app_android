package com.gigforce.app.modules.gigerVerfication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GigerVerificationViewModelFactory constructor(context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GigVerificationViewModel::class.java))
            return GigVerificationViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}