package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ViewFullScreenImageDialogFragment
import com.gigforce.common_ui.ViewFullScreenVideoDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.DownloadCompleted
import com.gigforce.modules.feature_chat.DownloadStarted
import com.gigforce.modules.feature_chat.ErrorWhileDownloadingAttachment
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatGroup
import com.gigforce.modules.feature_chat.models.ContactModel
import com.gigforce.modules.feature_chat.models.GroupMedia
import com.gigforce.modules.feature_chat.screens.adapters.GroupMediaRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.GroupMembersRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_chat_group_details_main_2.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class GroupDetailsFragment : Fragment(),
        PopupMenu.OnMenuItemClickListener,
        GroupMediaRecyclerAdapter.OnGroupMediaClickListener,
        GroupMembersRecyclerAdapter.OnGroupMembersClickListener,
        OnContactsSelectedListener {

    @Inject
    lateinit var navigation: IChatNavigation

    private val viewModel: GroupChatViewModel by lazy {
        ViewModelProvider(this, GroupChatViewModelFactory(requireContext())).get(GroupChatViewModel::class.java)
    }
    private lateinit var groupId: String
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (this.requireContext().applicationContext as ChatModuleProvider)
                .provideChatModule()
                .inject(this)
        navigation.context = requireContext()
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

//        viewModel.chatAttachmentDownloadState.observe(viewLifecycleOwner, Observer {
//            it ?: return@Observer
//
//            when (it) {
//                is DownloadStarted -> {
//                    groupMediaRecyclerAdapter.setItemAsDownloading(it.index)
//                }
//                is DownloadCompleted -> {
//                    groupMediaRecyclerAdapter.notifyItemChanged(it.index)
//                }
//                is ErrorWhileDownloadingAttachment -> {
//                    groupMediaRecyclerAdapter.setItemAsNotDownloading(it.index)
//                }
//            }
//        })

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
                                    .setTitle("Unable to activate/deactivate group")
                                    .setPositiveButton("Okay") { _, _ -> }
                                    .show()
                        }
                    }
                })

        viewModel.setGroupId(groupId)
        viewModel.startWatchingGroupDetails()
    }

    private fun showGroupDetails(content: ChatGroup) {
        group_name_tv.text = content.name
        group_creation_date_tv.text = "Created On : ${dateFormatter.format(content.creationDetails!!.createdOn.toDate())}"

        media_count_tv.text = content.groupMedia.size.toString()
        gigers_count_tv.text = content.groupMembers.size.toString()

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
                deactivate_group_btn.text = "Activate Group"
                group_deactivated_container.visible()
            } else {
                deactivate_group_btn.text = "Deactivate Group"
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

    private fun showDownloadingDialog() {
//        UtilMethods.showLoading(requireContext())
    }

    private fun openDocument(file: File) {

        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                    FileProvider.getUriForFile(
                            requireContext(),
                            "com.gigforce.app.provider",
                            file
                    ), "application/pdf"
            )
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                requireContext().startActivity(this)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open", Toast.LENGTH_SHORT).show()
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

        media_forward_iv.setOnClickListener {

            navigation.openGroupMediaList(
                    groupId
            )
        }

        edit_group_name_iv.setOnClickListener {

            val groupNameEt = EditText(requireContext())

            val layout = FrameLayout(requireContext())
            layout.setPaddingRelative(45, 15, 45, 0)
            layout.addView(groupNameEt)

            MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Enter a new group name")
                    .setTitle("Change group name")
                    .setView(layout)
                    .setPositiveButton("Okay") { _, _ ->

                        if (groupNameEt.length() == 0) {
                            Toast.makeText(requireContext(), "Please enter a valid group name", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.changeGroupName(groupNameEt.text.toString().capitalize())
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_message_user -> {

            contactLongPressed?.let {

                navigation.navigateToChatPage(
                        chatType = ChatConstants.CHAT_TYPE_USER,
                        otherUserId = it.uid!!,
                        headerId = "",
                        otherUserName = it.name ?: "",
                        otherUserProfilePicture = it.imageUrl ?: ""
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

        contactLongPressed = contact
        val popUp = PopupMenu(requireContext(), view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_group_members_long_click)
        popUp.show()
    }

    override fun onChatIconClicked(position: Int, contact: ContactModel) {

        navigation.navigateToChatPage(
                chatType = ChatConstants.CHAT_TYPE_USER,
                otherUserId = contact.uid!!,
                headerId = "",
                otherUserName = contact.name ?: "",
                otherUserProfilePicture = contact.imageUrl ?: ""
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