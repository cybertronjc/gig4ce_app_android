package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.StringConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.databinding.UserAndGroupDetailsFragmentBinding
import com.gigforce.modules.feature_chat.screens.ContactsAndGroupFragment.Companion.INTENT_EXTRA_RETURN_SELECTED_RESULTS
import com.gigforce.modules.feature_chat.screens.adapters.GroupMediaRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.GroupMembersRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UserAndGroupDetailsFragment : BaseFragment2<UserAndGroupDetailsFragmentBinding>(
    fragmentName = "UserAndGroupDetailsFragment",
    layoutId = R.layout.user_and_group_details_fragment,
    statusBarColor = R.color.lipstick_2
), GroupMediaRecyclerAdapter.OnGroupMediaClickListener, OnContactsSelectedListener, PopupMenu.OnMenuItemClickListener{

    companion object  {
        fun newInstance() = UserAndGroupDetailsFragment()
        const val TAG = "UserAndGroupDetailsFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
        const val INTENT_EXTRA_CHAT_HEADER_ID = "chat_header_id"
        const val INTENT_EXTRA_CHAT_TYPE = "chat_type"
        const val INTENT_EXTRA_OTHER_USER_ID = "sender_id"
        const val INTENT_EXTRA_OTHER_USER_NAME = "sender_name"
        const val INTENT_EXTRA_OTHER_USER_IMAGE = "sender_profile"
    }

    private val viewModel: GroupChatViewModel by viewModels()

    private val chatViewModel: ChatPageViewModel by viewModels()

    //Info specific to one to one chat
    //-------------------------------------
    private var receiverUserId: String? = null
    private var receiverName: String? = null
    private var receiverMobileNumber: String? = null
    private var receiverPhotoUrl: String? = null
    private var fromClientActivation: Boolean = false

    var onContactSelectedListener: OnContactsSelectedListener? = null

//    private var onContactClickListener : OnMembersClickListener? = null
//
//    fun setOnMembersClickedListener(listener: OnMembersClickListener){
//        this.onContactClickListener = listener
//    }

    @Inject
    lateinit var navigation: INavigation

    private var chatType: String = ChatConstants.CHAT_TYPE_USER
    private var chatHeaderOrGroupId: String? = null

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    @Inject
    lateinit var chatFileManager: ChatFileManager

    private lateinit var groupId: String
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
            requireContext(),
            chatFileManager.gigforceDirectory,
            Glide.with(requireContext()),
            this
        )
    }

    private val currentUserUid: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

//    private val groupMembersRecyclerAdapter: GroupMembersRecyclerAdapter by lazy {
//        GroupMembersRecyclerAdapter(
//            Glide.with(requireContext()),
//            this
//        )
//    }


    override fun viewCreated(
        viewBinding: UserAndGroupDetailsFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntents(arguments, savedInstanceState)
        setListeners()
        initRecyclerView()
        checkForChatTypeAndSubscribeToRespectiveViewModel()
        setStatusBarIcons(false)
    }

    private fun initRecyclerView() = viewBinding.apply{
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mediaRecyclerview.layoutManager = layoutManager
        mediaRecyclerview.adapter = groupMediaRecyclerAdapter

        val membersLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        membersRecyclerview.layoutManager = membersLayoutManager
//        membersRecyclerview.isNestedScrollingEnabled = false
//        membersRecyclerview.adapter = groupMembersRecyclerAdapter
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            //groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
            chatType = it.getString(INTENT_EXTRA_CHAT_TYPE)
                ?: throw IllegalArgumentException("please provide INTENT_EXTRA_CHAT_TYPE in intent extra")
            receiverPhotoUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            receiverName = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderOrGroupId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            receiverUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID) ?: ""
            receiverMobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
        }

        savedInstanceState?.let {
            //groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
            chatType = it.getString(INTENT_EXTRA_CHAT_TYPE)
                ?: throw IllegalArgumentException("please provide INTENT_EXTRA_CHAT_TYPE in intent extra")
            receiverPhotoUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            receiverName = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderOrGroupId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            receiverUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID) ?: ""
            receiverMobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(INTENT_EXTRA_OTHER_USER_IMAGE, receiverPhotoUrl)
            putString(INTENT_EXTRA_OTHER_USER_NAME, receiverName)
            putString(INTENT_EXTRA_CHAT_HEADER_ID, chatHeaderOrGroupId)
            putString(INTENT_EXTRA_CHAT_TYPE, chatType)
            putString(INTENT_EXTRA_OTHER_USER_ID, receiverUserId)
            putString(StringConstants.MOBILE_NUMBER.value, receiverMobileNumber)
        }
    }

    private fun setListeners() = viewBinding.apply{


        //block user
        blockUserLayout.setOnClickListener {
            BlockUserBottomSheetFragment.launch(
                chatViewModel.headerId,
                chatViewModel.otherUserId,
                childFragmentManager
            )
        }

        //report user
        reportUserLayout.setOnClickListener {
            ReportUserBottomSheetFragment.launch(
                chatViewModel.headerId,
                chatViewModel.otherUserId,
                childFragmentManager
            )
        }

        backButtonDetails.setOnClickListener {
            navigation.popBackStack()
        }

        forwardArrow.setOnClickListener {
//            navigation.navigateTo("chats/mediaAndDocsFragment", bundleOf(
//                INTENT_EXTRA_GROUP_ID to chatHeaderOrGroupId
//            ))
            chatNavigation.openGroupMediaList(
                chatHeaderOrGroupId.toString()
            )
        }

        addGigerLayout.setOnClickListener {
//            navigation.navigateTo("chats/contactsFragment", bundleOf(
//                INTENT_EXTRA_RETURN_SELECTED_RESULTS to true
//            ))
            ContactsFragment.launchForSelectingContact(childFragmentManager, this@UserAndGroupDetailsFragment)
        }

        addContactFab.setOnClickListener {
//            navigation.navigateTo("chats/contactsFragment", bundleOf(
//                INTENT_EXTRA_RETURN_SELECTED_RESULTS to true
//            ))
            ContactsFragment.launchForSelectingContact(childFragmentManager, this@UserAndGroupDetailsFragment)
        }
    }

    private fun showMembers(members: List<ContactModel>) = viewBinding.apply{
        membersLinearLayout.removeAllViewsInLayout()
        members.forEachIndexed { index, contact ->
            Log.d("selected", "$contact")
            val itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item_group_member_2, null)

            val contactAvatarIV: GigforceImageView = itemView.findViewById(R.id.user_image_iv)
            val contactNameTV: TextView = itemView.findViewById(R.id.user_name_tv)
            val uidTV: TextView = itemView.findViewById(R.id.last_online_time_tv)
            val chatOverlay: View = itemView.findViewById(R.id.chat_overlay)
            val chatIcon: View = itemView.findViewById(R.id.chat_icon)

            contactNameTV.text = if (contact.isUserGroupManager) contact.name + "(Admin)" else contact.name
            uidTV.text = ""

            val isUserTheCurrentUser = contact.uid == currentUserUid
            if (isUserTheCurrentUser) {
                chatOverlay.gone()
                chatIcon.gone()
            } else {
                chatOverlay.visible()
                chatIcon.visible()
            }

            if (!contact.imageThumbnailPathInStorage.isNullOrBlank()) {

                if (Patterns.WEB_URL.matcher(contact.imageThumbnailPathInStorage!!).matches()) {
                    contactAvatarIV.loadImageIfUrlElseTryFirebaseStorage(contact.imageThumbnailPathInStorage!!)
                } else {

                    val profilePathRef = if (contact.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                        contact.imageThumbnailPathInStorage!!
                    else
                        "profile_pics/${contact.imageThumbnailPathInStorage}"

                    contactAvatarIV.loadImageFromFirebase(profilePathRef)
                }
            } else if (!contact.imagePathInStorage.isNullOrBlank()) {

                if (Patterns.WEB_URL.matcher(contact.imagePathInStorage!!).matches()) {
                    contactAvatarIV.loadImageIfUrlElseTryFirebaseStorage(contact.imagePathInStorage!!)
                } else {

                    val profilePathRef = if (contact.imagePathInStorage!!.startsWith("profile_pics/"))
                        contact.imagePathInStorage!!
                    else
                        "profile_pics/${contact.imagePathInStorage}"
                    contactAvatarIV.loadImageFromFirebase(profilePathRef)
                }
            } else {
                GlideApp.with(this@UserAndGroupDetailsFragment).load(R.drawable.ic_user_2).into(contactAvatarIV)
            }

            itemView.setOnLongClickListener {
                if (viewModel.isContactModelOfCurrentUser(contact))
                    this

                contactLongPressed = contact
                val popUp = PopupMenu(context, itemView)
                popUp.inflate(R.menu.menu_group_members_long_click)

                if (viewModel.isUserGroupAdmin()) {
                    popUp.menu.findItem(R.id.action_remove_user).also { item ->
                        item.isVisible = true
                        item.title = getString(R.string.remove_chat) + " "+ contact.name
                    }
                } else {
                    popUp.menu.findItem(R.id.action_remove_user).also {
                        it.isVisible = false
                    }
                }

                if (viewModel.isUserGroupAdmin()) {
                    popUp.menu.findItem(R.id.action_make_admin).also {
                        it.isVisible = true
                        it.title = if (contact.isUserGroupManager)
                            getString(R.string.dismiss_as_admin_chat)
                        else
                            getString(R.string.make_group_admin_chat)

                    }
                } else {
                    popUp.menu.findItem(R.id.action_make_admin).also {
                        it.isVisible = false
                    }
                }

                popUp.setOnMenuItemClickListener(this@UserAndGroupDetailsFragment)
                popUp.show()

                true
            }

            chatOverlay.setOnClickListener {
                val otherUserName = if (contact.name.isNullOrBlank()) {
                    contact.mobile
                } else
                    contact.name!!

                chatNavigation.navigateToChatPage(
                    chatType = ChatConstants.CHAT_TYPE_USER,
                    otherUserId = contact.uid!!,
                    headerId = "",
                    otherUserName = otherUserName,
                    otherUserProfilePicture = contact.getUserProfileImageUrlOrPath() ?: "",
                    sharedFileBundle = null
                )
            }

            membersLinearLayout.addView(itemView)
        }
    }

    private fun checkForChatTypeAndSubscribeToRespectiveViewModel() {

        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            chatViewModel.setRequiredDataAndStartListeningToMessages(
                otherUserId = receiverUserId!!,
                headerId = chatHeaderOrGroupId,
                otherUserName = receiverName,
                otherUserProfilePicture = receiverPhotoUrl,
                otherUserMobileNo = receiverMobileNumber
            )
            adjustUiAccToOneToOneChat()
            subscribeOneToOneViewModel()
        } else if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
            if (chatHeaderOrGroupId.isNullOrBlank()) {
                CrashlyticsLogger.e(
                    TAG,
                    "getting args from arguments",
                    Exception("$chatHeaderOrGroupId <-- String passed as groupId")
                )
                throw IllegalArgumentException("$chatHeaderOrGroupId <-- String passed as groupId")
            }
            adjustUiAccToGroupChat()
            subscribeChatGroupViewModel()
        }
    }

    private fun adjustUiAccToOneToOneChat() = viewBinding.apply{
        membersLayout.gone()
        exitGroupLayout.gone()
        blockUserLayout.visible()
        exitGroupLayout.visible()
        muteNotificationsLayout.gone()
        reportUserLayout.gone()
    }

    private fun adjustUiAccToGroupChat() = viewBinding.apply{
        membersLayout.visible()
        exitGroupLayout.visible()
        blockUserLayout.gone()
        exitGroupLayout.gone()
        muteNotificationsLayout.gone()
        reportUserLayout.gone()
    }

    private fun subscribeOneToOneViewModel() = viewBinding.apply{
        chatViewModel.otherUserInfo
            .observe(viewLifecycleOwner, Observer {

                if (it.name.isNullOrBlank()) {
                    overlayCardLayout.profileName.text = "Add new contact"
                } else {
                    overlayCardLayout.profileName.text = it.name ?: ""
                }
                overlayCardLayout.contactNumber.text = it.mobile ?: ""

                if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imageThumbnailPathInStorage!!).matches()) {
                        overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(it.imageThumbnailPathInStorage!!, R.drawable.ic_user_white)

                    } else {

                        val profilePathRef =
                            if (it.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                                it.imageThumbnailPathInStorage!!
                            else
                                "profile_pics/${it.imageThumbnailPathInStorage}"

                        overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(profilePathRef, R.drawable.ic_user_white)
                    }
                } else if (!it.imagePathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imagePathInStorage!!).matches()) {
                        overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(it.imagePathInStorage!!, R.drawable.ic_user_white)

                    } else {

                        val profilePathRef =
                            if (it.imagePathInStorage!!.startsWith("profile_pics/"))
                                it.imagePathInStorage!!
                            else
                                "profile_pics/${it.imagePathInStorage}"

                        overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(profilePathRef, R.drawable.ic_user_white)
                    }

                } else {

                    overlayCardLayout.profileImg.loadImage(R.drawable.ic_user_white)
                }

                if (it.isUserBlocked) {
                    blockText.text = "Unblock"
                } else {
                    blockText.text = "Block"
                }
            })

    }

    private fun subscribeChatGroupViewModel() {
        viewModel
            .groupInfo.observe(viewLifecycleOwner, Observer {
                showGroupDetails(it)
            })

        viewModel.chatAttachmentDownloadState.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            when (it) {
                is DownloadStarted -> {
                    groupMediaRecyclerAdapter.setItemAsDownloading(it.index)
                }
                is DownloadCompleted -> {
                    groupMediaRecyclerAdapter.notifyItemChanged(it.index)
                }
                is ErrorWhileDownloadingAttachment -> {
                    groupMediaRecyclerAdapter.setItemAsNotDownloading(it.index)
                }
            }
        })

        viewModel.deactivatingGroup
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lse.Loading -> {

                    }
                    Lse.Success -> {

                    }
                    is Lse.Error -> {
//                            UtilMethods.hideLoading()

                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(it.error)
                            .setTitle(getString(R.string.unable_to_activate_group_chat))
                            .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                            .show()
                    }
                }
            })

        if (chatHeaderOrGroupId?.isEmpty() == true) {
            CrashlyticsLogger.e(GroupDetailsFragment.TAG, "getting args from arguments", Exception("$chatHeaderOrGroupId <-- String passed as groupId"))
            throw IllegalArgumentException("$chatHeaderOrGroupId <-- String passed as groupId")
        }

        chatHeaderOrGroupId?.let { viewModel.setGroupId(it) }
        viewModel.startWatchingGroupDetails()
    }

    private fun showGroupDetails(content: ChatGroup) = viewBinding.apply{
        overlayCardLayout.profileName.text = content.name
        if (content.groupAvatarThumbnail.isNotBlank()) {
            content.groupAvatarThumbnail.let {
                overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(it, R.drawable.ic_group)
            }
        } else if (content.groupAvatar.isNotBlank()) {
            content.groupAvatar.let {
                overlayCardLayout.profileImg.loadImageIfUrlElseTryFirebaseStorage(it, R.drawable.ic_group)
            }
        } else {
            overlayCardLayout.profileImg.loadImage(R.drawable.ic_group, true)
            //toolbar.showImageBehindBackButton(R.drawable.ic_group_white)
        }

        overlayCardLayout.contactNumber.text = getString(R.string.created_by_chat) + " " + content.creationDetails?.creatorName ?: ""
//        group_creation_date_tv.text =
//            getString(R.string.created_on_chat) + dateFormatter.format(content.creationDetails!!.createdOn.toDate())
        Log.d("profileName", "media size: ${content.groupMedia.size}, members: ${content.groupMembers.size}")
        mediaAndDocsCount.text = content.groupMedia.size.toString()
        participantsCount.text = content.groupMembers.size.toString() + " "+ getString(R.string.participants_chat)

//        group_details_divider_0.isVisible = viewModel.isUserGroupAdmin()
//        group_write_controls_layout.isVisible = viewModel.isUserGroupAdmin()
//
//        if (content.onlyAdminCanPostInGroup && only_admins_can_post_switch.isChecked.not()) {
//            only_admins_can_post_switch.isChecked = true
//        } else if (content.onlyAdminCanPostInGroup.not() && only_admins_can_post_switch.isChecked) {
//            only_admins_can_post_switch.isChecked = false
//        }

        if (content.groupMedia.isEmpty()) {
            mediaLayout.gone()
        } else {
            mediaLayout.visible()
        }
        groupMediaRecyclerAdapter.setData(content.groupMedia)

        val membersList = content.groupMembers.filter {
            it.isUserGroupManager
        }.sortedBy {
            it.name
        }

        val nonMgrMembers = content.groupMembers.filter {
            !it.isUserGroupManager
        }.sortedBy {
            it.name
        }

//        groupMembersRecyclerAdapter.setData(
//            membersList.plus(nonMgrMembers)
//        )

        showMembers(membersList.plus(nonMgrMembers))

        showOrHideAddGroupOption(content)
    }

    private fun showOrHideAddGroupOption(content: ChatGroup) = viewBinding.apply {
        val groupManagers = content.groupMembers.filter {
            it.isUserGroupManager
        }

        var isUserGroupManager = false
        for (member in groupManagers) {

            if (member.uid == currentUserUid) {
                isUserGroupManager = true
                break
            }
        }
        //groupMembersRecyclerAdapter.setOrRemoveUserAsGroupManager(isUserGroupManager)

        if (isUserGroupManager) {
            if (content.groupDeactivated) {
                addGigerLayout.isVisible = false
                activateGroupLayout.isVisible = true
                activateText.text = getString(R.string.activate_group_chat)
                //group_deactivated_container.visible()
            } else {
                activateText.text = getString(R.string.deactivate_group_chat)
                addGigerLayout.isVisible = true
                activateGroupLayout.isVisible = true
                //group_deactivated_container.gone()
            }
        } else {
            addGigerLayout.isVisible = false
            activateText.isVisible = false
        }

        if (content.groupDeactivated) {
            activateGroupLayout.visible()
        } else {
            activateGroupLayout.gone()
        }
    }



    override fun onChatMediaClicked(
        position: Int,
        fileDownloaded: Boolean,
        fileIfDownloaded: File?,
        media: GroupMedia
    ) {
        if (fileDownloaded) {
            //Open the file
                Log.d("type", "type : ${media.attachmentType}")
            when (media.attachmentType) {
                ChatConstants.ATTACHMENT_TYPE_IMAGE -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenImageViewDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.ATTACHMENT_TYPE_VIDEO -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenVideoDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.ATTACHMENT_TYPE_DOCUMENT -> {
                    openDocument(fileIfDownloaded!!)
                }
                ChatConstants.ATTACHMENT_TYPE_AUDIO -> {
                    openAudioPlayerBottomSheet(fileIfDownloaded!!)
                }
            }

        } else {
            //Start downloading the file
            viewModel.downloadAndSaveFile(chatFileManager.gigforceDirectory, position, media)
        }
    }

    private fun openAudioPlayerBottomSheet(file: File) {
        navigation.navigateTo(
            "chats/audioPlayer", bundleOf(
                AudioPlayerBottomSheetFragment.INTENT_EXTRA_URI to file.path!!
            )
        )
    }

    private fun openDocument(file: File) {

        Intent(Intent.ACTION_VIEW).apply {

            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                file
            )
            setDataAndType(
                uri,
                ImageMetaDataHelpers.getImageMimeType(
                    requireContext(),
                    file.toUri()
                )
            )

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                requireContext().startActivity(this)
            } catch (e: Exception) {
                showToast("Unable to open document")
            }
        }
    }

    private var contactLongPressed: ContactModel? = null

    override fun onContactsSelected(contacts: List<ContactModel>) {
        viewModel.addUsersGroup(contacts)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {

            R.id.action_message_user -> {

                contactLongPressed?.let {
                    val otherUserName = if (it.name.isNullOrBlank()) {
                        it.mobile
                    } else
                        it.name!!

                    chatNavigation.navigateToChatPage(
                        chatType = ChatConstants.CHAT_TYPE_USER,
                        otherUserId = it.uid!!,
                        headerId = "",
                        otherUserName = otherUserName,
                        otherUserProfilePicture = it.imageUrl ?: "",
                        sharedFileBundle = null
                    )
                }
                contactLongPressed = null
                true
            }
            R.id.action_remove_user -> {
                contactLongPressed?.let {
                    viewModel.removeUserFromGroup(it.uid!!)
                }
                contactLongPressed = null
                true
            }
            R.id.action_make_admin -> {
                contactLongPressed?.let {

                    if (it.isUserGroupManager)
                        viewModel.dismissAsGroupAdmin(it.uid!!)
                    else
                        viewModel.makeUserGroupAdmin(it.uid!!)

                    contactLongPressed = null
                }
                true
            }
            else -> {
                false
            }
        }
        return true
    }

    override fun onStop() {
        setStatusBarIcons(true)
        super.onStop()
    }

    fun setStatusBarIcons(shouldChangeStatusBarTintToDark: Boolean){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor: View = activity?.window?.decorView!!
            if (shouldChangeStatusBarTintToDark) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // We want to change tint color to white again.
                // You can also record the flags in advance so that you can turn UI back completely if
                // you have set other flags before, such as translucent or full screen.
                decor.systemUiVisibility = 0
            }
        }
    }

}
