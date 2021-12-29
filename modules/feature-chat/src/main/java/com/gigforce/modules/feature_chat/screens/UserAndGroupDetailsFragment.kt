package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.databinding.UserAndGroupDetailsFragmentBinding
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
), GroupMediaRecyclerAdapter.OnGroupMediaClickListener,
    GroupMembersRecyclerAdapter.OnGroupMembersClickListener {

    companion object {
        fun newInstance() = UserAndGroupDetailsFragment()
        const val TAG = "UserAndGroupDetailsFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }

    private val viewModel: GroupChatViewModel by viewModels()

    private val chatViewModel: ChatPageViewModel by viewModels()

    @Inject
    lateinit var navigation: INavigation

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

    private val groupMembersRecyclerAdapter: GroupMembersRecyclerAdapter by lazy {
        GroupMembersRecyclerAdapter(
            Glide.with(requireContext()),
            this
        )
    }


    override fun viewCreated(
        viewBinding: UserAndGroupDetailsFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntents(arguments, savedInstanceState)
        initToolbar()
        setListeners()
        initRecyclerView()
        initViewModel()
    }

    private fun initRecyclerView() = viewBinding.apply{
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mediaRecyclerview.layoutManager = layoutManager
        mediaRecyclerview.adapter = groupMediaRecyclerAdapter

        val membersLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        membersRecyclerview.layoutManager = membersLayoutManager
        membersRecyclerview.isNestedScrollingEnabled = false
        membersRecyclerview.adapter = groupMembersRecyclerAdapter
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }

        savedInstanceState?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }
    }

    private fun initToolbar() = viewBinding.apply {
        toolbar.hideActionMenu()
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
    }

    private fun initViewModel() {
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

        if (groupId.isEmpty()) {
            CrashlyticsLogger.e(GroupDetailsFragment.TAG, "getting args from arguments", Exception("$groupId <-- String passed as groupId"))
            throw IllegalArgumentException("$groupId <-- String passed as groupId")
        }

        viewModel.setGroupId(groupId)
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
            mediaRecyclerview.gone()
        } else {
            mediaRecyclerview.visible()
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

        //showOrHideAddGroupOption(content)
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
            }

        } else {
            //Start downloading the file
            viewModel.downloadAndSaveFile(chatFileManager.gigforceDirectory, position, media)
        }
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

    override fun onGroupMemberItemLongPressed(view: View, position: Int, contact: ContactModel) {

    }

    override fun onChatIconClicked(position: Int, contact: ContactModel) {

    }

}