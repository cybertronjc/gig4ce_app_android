package com.gigforce.app.di.implementations

import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.core.navigation.BaseNavigationImpl
import com.gigforce.modules.feature_chat.core.IChatNavigation
import javax.inject.Inject

class ChatNavigationImpl @Inject constructor()
    : BaseNavigationImpl(),
    IChatNavigation {

    override fun navigateToChatList() {

    }

    override fun navigateToChatPage(
            otherUserId: String,
            headerId: String,
            otherUserName: String,
            otherUserProfilePicture: String
    ) {

        Log.i("Chat/Nav/Impl","Navigate to Chat Page Tapped")
        Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()
        (this.context as MainActivity).getNavController().navigate(R.id.chatPageFragment, bundleOf(

        ))
    }
}