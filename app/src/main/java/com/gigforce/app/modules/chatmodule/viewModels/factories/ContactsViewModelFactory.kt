package com.vinners.cmi.ui.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.modules.chatmodule.SyncPref
import com.gigforce.app.modules.chatmodule.repository.ChatContactsRepository
import com.gigforce.app.modules.chatmodule.viewModels.ContactsViewModel
import com.gigforce.app.utils.network.RetrofitFactory
import com.google.firebase.storage.FirebaseStorage

class ContactsViewModelFactory constructor(context: Context) : ViewModelProvider.Factory {

    private val chatContactsRepository = ChatContactsRepository(
        syncPref = SyncPref.getInstance(context.applicationContext),
        firebaseStorage = FirebaseStorage.getInstance(),
        syncContactsService = RetrofitFactory.generateSyncContactsService()
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java))
            return ContactsViewModel(
                chatContactsRepository
            ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}