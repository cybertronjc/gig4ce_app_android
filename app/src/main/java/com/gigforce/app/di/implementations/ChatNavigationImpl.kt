package com.gigforce.app.di.implementations

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.gigforce.modules.feature_chat.IChatNavigation
import javax.inject.Inject

class ChatNavigationImpl @Inject constructor(val context: Context)
    : IChatNavigation {

    override fun navigateToChatList() {

    }

    override fun navigateToChatPage(id: String) {
        Log.i("Chat/Nav/Impl","Navigate to Chat Page Tapped")
        Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()
    }
}