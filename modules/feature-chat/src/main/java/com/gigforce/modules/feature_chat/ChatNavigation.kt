package com.gigforce.modules.feature_chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.navOptions
import com.gigforce.common_ui.ViewFullScreenImageDialogFragment
import com.gigforce.common_ui.ViewFullScreenVideoDialogFragment
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.gigforce.modules.feature_chat.screens.GroupDetailsFragment
import com.gigforce.modules.feature_chat.screens.GroupMediaListFragment2

class ChatNavigation(
        private val iNavigation: INavigation
) {


    fun navigateToChatList() {

    }

    fun navigateBackToChatListIfExistElseOneStepBack() {

        try {
            iNavigation.getBackStackEntry("chats/chatList")
            iNavigation.popBackStack("chats/chatList", false)
        } catch (e: Exception) {
            iNavigation.navigateUp()
        }
    }

    fun navigateToGroupChat(headerId: String) {
        Log.i("Chat/Nav/Impl", "Navigate to group chat Page Tapped")

        iNavigation.navigateTo(
                dest = "chats/chatPage",
                bundleOf(
                        ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                        ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID to headerId
                )
        )
    }

    fun navigateToChatPage(
            chatType: String,
            otherUserId: String,
            headerId: String,
            otherUserName: String,
            otherUserProfilePicture: String,
            sharedFileBundle: Bundle?,
            cameFromLinkInOtherChat : Boolean = false
    ) {

        Log.i("Chat/Nav/Impl", "Navigate to Chat Page Tapped")
        // Toast.makeText(context, "Navigate to Chat Page Tapped", Toast.LENGTH_LONG).show()

        iNavigation.navigateTo(
                dest = "chats/chatPage",
                bundleOf(
                        ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to chatType,
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to otherUserId,
                        ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID to headerId,
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to otherUserName,
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to otherUserProfilePicture,
                        ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to sharedFileBundle,
                        ChatPageFragment.INTENT_EXTRA_CAME_FROM_LINK_IN_OTHER_CHAT to cameFromLinkInOtherChat
                ),
                navOptions {
                    this.anim {
                        this.enter = R.anim.nav_default_enter_anim
                        this.exit = R.anim.nav_default_exit_anim
                        this.popEnter= R.anim.nav_default_pop_enter_anim
                        this.popExit= R.anim.nav_default_pop_exit_anim
                    }
                }
        )
    }

    fun navigateToContactsPage(
            bundle: Bundle?
    ) {

        iNavigation.navigateTo(
                dest = "chats/contacts",
                args = bundle
        )
    }

    fun openFullScreenImageViewDialogFragment(
            uri: Uri
    ) {
        try {

            val currentDestination = iNavigation.getCurrentDestination()?.label
            if("ViewFullScreenImageDialogFragment" == currentDestination) return

            iNavigation.navigateTo(
                    dest = "common/viewImageFullScreen",
                    args = bundleOf(
                            ViewFullScreenImageDialogFragment.INTENT_EXTRA_IMAGE_URI to uri.toString()
                    )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun openFullScreenVideoDialogFragment(uri: Uri) {

        iNavigation.navigateTo(
                dest = "common/viewVideoFullScreen",
                args = bundleOf(
                        ViewFullScreenVideoDialogFragment.INTENT_EXTRA_URI to uri.toString()
                )
        )
    }

    fun openInviteAFriendFragment() {

        iNavigation.navigateTo(
                dest = "common/invite_friend"
        )
    }

    fun openGroupDetailsPage(groupId: String) {

        iNavigation.navigateTo(
                dest = "chats/groupDetails",
                args = bundleOf(GroupDetailsFragment.INTENT_EXTRA_GROUP_ID to groupId)
        )
    }

    fun openGroupMediaList(groupId: String) {

        iNavigation.navigateTo(
                dest = "chats/groupMediaList",
                args = bundleOf(GroupMediaListFragment2.INTENT_EXTRA_GROUP_ID to groupId)
        )
    }

    fun navigateUp(){
        iNavigation.navigateUp()
    }

}