package com.gigforce.modules.feature_chat.screens.vm.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.screens.vm.NewContactsViewModel
import com.gigforce.modules.feature_chat.service.SyncPref

class NewContactsViewModelFactory constructor(context: Context) : ViewModelProvider.Factory {

    private val chatContactsRepository = ChatContactsRepository(
            syncPref = SyncPref.getInstance(context.applicationContext)/*,
        firebaseStorage = FirebaseStorage.getInstance()*/
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewContactsViewModel::class.java))
            return NewContactsViewModel(
                    chatContactsRepository
            ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}