package com.gigforce.modules.feature_chat.core

import android.content.Context
import android.net.Uri
import com.gigforce.core.navigation.INavigation

interface IChatNavigation: INavigation {

    fun navigateToChatList()

    fun navigateToChatPage(
            otherUserId : String,
            headerId:String ="",
            otherUserName : String = "",
            otherUserProfilePicture : String = ""
    )

    fun navigateToContactsPage()

    fun openFullScreenImageViewDialogFragment(
        uri : Uri
    )

    fun openFullScreenVideoDialogFragment(
        uri : Uri
    )


}