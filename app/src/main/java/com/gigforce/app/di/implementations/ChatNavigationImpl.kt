package com.gigforce.app.di.implementations

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.modules.feature_chat.core.IChatNavigation
import javax.inject.Inject

class ChatNavigationImpl @Inject constructor(
)
    : BaseNavigationImpl(),
    IChatNavigation {

    override fun navigateToChatList() {

    }

    override fun getNavController(context: Context): NavController {
        return (context as MainActivity).getNavController()
    }

    override fun RegisterAllRoutes() {
        
    }

    override fun navigateToChatPage(id: String) {
        Log.i("Chat/Nav/Impl","Navigate to Chat Page Tapped")
        //Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()
        //(this.context as MainActivity).getNavController().navigate(R.id.application_questionnaire)
    }
}