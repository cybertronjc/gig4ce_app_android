package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ViewFullScreenImageDialogFragment
import com.gigforce.common_ui.ViewFullScreenVideoDialogFragment
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.screens.adapters.GroupMediaRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.GroupMembersRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat_group_details_main_2.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GroupDetailsFragment : Fragment(),
        PopupMenu.OnMenuItemClickListener,
        GroupMediaRecyclerAdapter.OnGroupMediaClickListener,
        GroupMembersRecyclerAdapter.OnGroupMembersClickListener,
        OnContactsSelectedListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val viewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
                this,
                GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }
    private lateinit var groupId: String
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
                requireContext(),
                appDirectoryFileRef,
                Glide.with(requireContext()),
                this
        )
    }

    private val currentUserUid: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private val groupMembersRecyclerAdapter: GroupMembersRecyclerAdapter by lazy {
        GroupMembersRecyclerAdapter(
                Glide.with(requireContext()),
                this
        )
    }

    private val appDirectoryFileRef: File by lazy {
        Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_group_details_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        init()
        subscribeViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GROUP_ID, groupId)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }

        savedInstanceState?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }
    }

    private fun init() {
        initListeners()
        initRecycler()
    }

    private fun initRecycler() {
        val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        media_recyclerview.layoutManager = layoutManager
        media_recyclerview.adapter = groupMediaRecyclerAdapter

        val membersLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        user_list_recyclerview.layoutManager = membersLayoutManager
        user_list_recyclerview.adapter = groupMembersRecyclerAdapter
    }

    private fun subscribeViewModel() {

        viewModel
                .groupInfo
                .observe(viewLifecycleOwner, Observer {
                    showGroupDetails(it)

//                    when (it) {
//                        Lce.Loading -> {
//                            group_chat_main.gone()
//                            group_chat_error.gone()
//                            group_chat_progress_bar.visible()
//                        }
//                        is Lce.Content -> {
//                            group_chat_progress_bar.gone()
//                            group_chat_error.gone()
//                            group_chat_main.visible()
//
//
//                        }
//                        is Lce.Error -> {
//                            group_chat_progress_bar.gone()
//                            group_chat_main.gone()
//
//                            group_chat_error.visible()
//                            group_chat_error.text = it.error
//                        }
//                    }
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

        if (groupId.isEmpty()) {
            CrashlyticsLogger.e(TAG, "getting args from arguments", Exception("$groupId <-- String passed as groupId"))
            throw IllegalArgumentException("$groupId <-- String passed as groupId")
        }

        viewModel.setGroupId(groupId)
        viewModel.startWatchingGroupDetails()
    }

    private fun showGroupDetails(content: ChatGroup) {
        group_name_tv.text = content.name
        group_creation_date_tv.text =
            getString(R.string.created_on_chat) + dateFormatter.format(content.creationDetails!!.createdOn.toDate())

        media_count_tv.text = content.groupMedia.size.toString()
        gigers_count_tv.text = content.groupMembers.size.toString()

        group_details_divider_0.isVisible = viewModel.isUserGroupAdmin()
        group_write_controls_layout.isVisible = viewModel.isUserGroupAdmin()

        if (content.onlyAdminCanPostInGroup && only_admins_can_post_switch.isChecked.not()) {
            only_admins_can_post_switch.isChecked = true
        } else if (content.onlyAdminCanPostInGroup.not() && only_admins_can_post_switch.isChecked) {
            only_admins_can_post_switch.isChecked = false
        }

        if (content.groupMedia.isEmpty()) {
            media_recyclerview.gone()
        } else {
            media_recyclerview.visible()
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

        groupMembersRecyclerAdapter.setData(
                membersList.plus(nonMgrMembers)
        )

        showOrHideAddGroupOption(content)
    }

    private fun showOrHideAddGroupOption(content: ChatGroup) {
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
        groupMembersRecyclerAdapter.setOrRemoveUserAsGroupManager(isUserGroupManager)

        if (isUserGroupManager) {
            if (content.groupDeactivated) {
                add_giger_layout.isVisible = false
                deactivate_group_btn.isVisible = true
                deactivate_group_btn.text = getString(R.string.activate_group_chat)
                group_deactivated_container.visible()
            } else {
                deactivate_group_btn.text = getString(R.string.deactivate_group_chat)
                add_giger_layout.isVisible = true
                deactivate_group_btn.isVisible = true
                group_deactivated_container.gone()
            }
        } else {
            add_giger_layout.isVisible = false
            deactivate_group_btn.isVisible = false
        }

        if (content.groupDeactivated) {
            group_deactivated_container.visible()
        } else {
            group_deactivated_container.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
                requireActivity(), ResourcesCompat.getColor(
                resources,
                android.R.color.white,
                null
        )
        )
    }

    private fun openDocument(file: File) {

        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                    FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            file
                    ), "application/pdf"
            )
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                requireContext().startActivity(this)
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.unable_to_open_chat), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showErrorDialog(error: String) {

    }

    private fun initListeners() {
        add_giger_layout.setOnClickListener {
            ContactsFragment.launchForSelectingContact(childFragmentManager, this)
        }

        add_contact_fab.setOnClickListener {
            ContactsFragment.launchForSelectingContact(childFragmentManager, this)
        }

        search_textview.onTextChanged {
            groupMembersRecyclerAdapter.filter.filter(it)
        }

        back_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        deactivate_group_btn.setOnClickListener {
            viewModel.deactivateOrActivateGroup()
        }

        media_title_layout.setOnClickListener {

            chatNavigation.openGroupMediaList(
                    groupId
            )
        }

        edit_group_name_iv.setOnClickListener {

            val groupNameEt = EditText(requireContext())
            groupNameEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(25))

            val layout = FrameLayout(requireContext())
            layout.setPaddingRelative(45, 15, 45, 0)
            layout.addView(groupNameEt)

            MaterialAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.enter_new_group_name_chat))
                    .setTitle(getString(R.string.change_group_name_chat))
                    .setView(layout)
                    .setPositiveButton(getString(R.string.okay_chat)) { _, _ ->

                        if (groupNameEt.length() == 0) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.enter_valid_group_name_chat),
                                    Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.changeGroupName(groupNameEt.text.toString().capitalize())
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel_chat)) { _, _ ->

                    }.show()
        }



        only_admins_can_post_switch.setOnCheckedChangeListener { _, isChecked ->
            val currentGroup = viewModel.getCurrentChatGroupInfo()
                    ?: return@setOnCheckedChangeListener
            if (isChecked && currentGroup.onlyAdminCanPostInGroup.not()) {
                viewModel.limitPostingToAdminsInGroup()
                showToast(getString(R.string.post_limited_to_admin_chat))
            } else if (!isChecked && currentGroup.onlyAdminCanPostInGroup) {
                viewModel.allowEveryoneToPostInThisGroup()
                showToast(getString(R.string.everyone_can_post_in_group_chat))
            }
        }
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
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

    override fun onChatMediaClicked(
            position: Int,
            fileDownloaded: Boolean,
            fileIfDownloaded: File?,
            media: GroupMedia
    ) {

        if (fileDownloaded) {
            //Open the file
            when (media.attachmentType) {
                ChatConstants.ATTACHMENT_TYPE_IMAGE -> {
                    ViewFullScreenImageDialogFragment.showImage(
                            childFragmentManager,
                            fileIfDownloaded?.toUri()!!
                    )

                }
                ChatConstants.ATTACHMENT_TYPE_VIDEO -> {
                    ViewFullScreenVideoDialogFragment.launch(
                            childFragmentManager,
                            fileIfDownloaded?.toUri()!!
                    )
                }
                ChatConstants.ATTACHMENT_TYPE_DOCUMENT -> {
                    openDocument(fileIfDownloaded!!)
                }
            }

        } else {
            //Start downloading the file
            viewModel.downloadAndSaveFile(appDirectoryFileRef, position, media)
        }
    }

    private var contactLongPressed: ContactModel? = null
    override fun onGroupMemberItemLongPressed(view: View, position: Int, contact: ContactModel) {
        if (viewModel.isContactModelOfCurrentUser(contact))
            return

        contactLongPressed = contact
        val popUp = PopupMenu(requireContext(), view)
        popUp.inflate(R.menu.menu_group_members_long_click)

        if (viewModel.isUserGroupAdmin()) {
            popUp.menu.findItem(R.id.action_remove_user).also { item ->
                item.isVisible = true
                item.title = getString(R.string.remove_chat) + contact.name
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

        popUp.setOnMenuItemClickListener(this)
        popUp.show()
    }

    override fun onChatIconClicked(position: Int, contact: ContactModel) {

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

    override fun onContactsSelected(contacts: List<ContactModel>) {
        viewModel.addUsersGroup(contacts)
    }

    companion object {
        const val TAG = "GroupChatFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }

}