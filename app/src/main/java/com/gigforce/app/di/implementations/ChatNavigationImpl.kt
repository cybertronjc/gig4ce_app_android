package com.gigforce.app.di.implementations

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.gigforce.app.utils.ViewFullScreenVideoDialogFragment
import com.gigforce.core.navigation.BaseNavigationImpl
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.gigforce.modules.feature_chat.screens.GroupDetailsFragment
import com.gigforce.modules.feature_chat.screens.GroupMediaListFragment2
import javax.inject.Inject

class ChatNavigationImpl @Inject constructor() : BaseNavigationImpl(),
    IChatNavigation {

    override fun navigateToChatList() {

    }

    override fun navigateBackToChatListIfExistElseOneStepBack() {

        (this.context as MainActivity).getNavController().apply {

            try {
                this.getBackStackEntry(R.id.chatListFragment)
                this.popBackStack(R.id.chatListFragment,false)
            } catch (e: Exception) {
                this.navigateUp()
            }
        }
    }

    override fun navigateToGroupChat(headerId: String) {
        Log.i("Chat/Nav/Impl", "Navigate to Chat Page Tapped")
       // Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()

        (this.context as MainActivity).getNavController().navigate(
            R.id.chatPageFragment, bundleOf(
                ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID to headerId
            )
        )
    }

    override fun navigateToChatPage(
        chatType: String,
        otherUserId: String,
        headerId: String,
        otherUserName: String,
        otherUserProfilePicture: String
    ) {

        Log.i("Chat/Nav/Impl", "Navigate to Chat Page Tapped")
       // Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()

        (this.context as MainActivity).getNavController().navigate(
            R.id.chatPageFragment, bundleOf(
                ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to chatType,
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

    override fun openInviteAFriendFragment() {
        (this.context as MainActivity).getNavController().navigate(
                R.id.referrals_fragment
        )
    }

    override fun openGroupDetailsPage(groupId: String) {
        (this.context as MainActivity).getNavController().navigate(
                R.id.groupDetailsFragment2,
                bundleOf(GroupDetailsFragment.INTENT_EXTRA_GROUP_ID to groupId)
        )
    }

    override fun openGroupMediaList(groupId: String) {
        (this.context as MainActivity).getNavController().navigate(
                R.id.groupMediaListFragment2,
                bundleOf(GroupMediaListFragment2.INTENT_EXTRA_GROUP_ID to groupId)
        )


    }
}