package com.gigforce.app.di.implementations

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.gigforce.app.utils.ViewFullScreenVideoDialogFragment
import com.gigforce.core.navigation.BaseNavigationImpl
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import javax.inject.Inject

class ChatNavigationImpl @Inject constructor() : BaseNavigationImpl(),
    IChatNavigation {

    override fun navigateToChatList() {

    }

    override fun navigateToChatPage(
        otherUserId: String,
        headerId: String,
        otherUserName: String,
        otherUserProfilePicture: String
    ) {

        Log.i("Chat/Nav/Impl", "Navigate to Chat Page Tapped")
        Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()

        (this.context as MainActivity).getNavController().navigate(
            R.id.chatPageFragment, bundleOf(
                ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID to headerId,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to otherUserId,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to otherUserProfilePicture,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to otherUserName
            )
        )
    }

    override fun navigateToContactsPage() {
        (this.context as MainActivity).getNavController().navigate(R.id.contactsFragment)
    }

    override fun openFullScreenImageViewDialogFragment(
        uri: Uri
    ) {
        (this.context as MainActivity).getNavController().navigate(
            R.id.viewFullScreenImageDialogFragment,
            bundleOf(
                ViewFullScreenImageDialogFragment.INTENT_EXTRA_IMAGE_URI to uri.toString()
            )
        )
//        ViewFullScreenImageDialogFragment.showImage()
    }

    override fun openFullScreenVideoDialogFragment(uri: Uri) {
        (this.context as MainActivity).getNavController().navigate(
            R.id.viewFullScreenVideoDialogFragment,
            bundleOf(
                ViewFullScreenVideoDialogFragment.INTENT_EXTRA_URI to uri.toString()
            )
        )
    }
}