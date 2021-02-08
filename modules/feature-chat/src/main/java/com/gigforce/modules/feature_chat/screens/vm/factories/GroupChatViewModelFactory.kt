package com.vinners.cmi.ui.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gigforce.modules.feature_chat.repositories.ChatContactsRepository
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.service.SyncPref
import com.google.firebase.storage.FirebaseStorage

class GroupChatViewModelFactory constructor(context: Context) : ViewModelProvider.Factory {

    private val chatContactsRepository = ChatContactsRepository(
        syncPref = SyncPref.getInstance(context.applicationContext)/*,
        firebaseStorage = FirebaseStorage.getInstance(),
        syncContactsService = RetrofitFactory.generateSyncContactsService()*/
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupChatViewModel::class.java))
            return GroupChatViewModel(
                chatContactsRepository
            ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}