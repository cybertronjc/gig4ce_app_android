package com.gigforce.app.modules.chatmodule.ui

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
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.*
import com.gigforce.app.modules.chatmodule.models.ChatGroup
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.models.GroupMedia
import com.gigforce.app.modules.chatmodule.ui.adapters.GroupMediaRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.GroupMembersRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnGroupMembersClickListener
import com.gigforce.app.modules.chatmodule.viewModels.GroupChatViewModel
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.*
import com.gigforce.core.AppConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.vinners.cmi.ui.activity.GroupChatViewModelFactory
import kotlinx.android.synthetic.main.fragment_chat_group_details.*
import kotlinx.android.synthetic.main.fragment_chat_group_details_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class GroupDetailsFragment : BaseFragment(),
    PopupMenu.OnMenuItemClickListener,
    GroupMediaRecyclerAdapter.OnGroupMediaClickListener,
    OnGroupMembersClickListener,
    OnContactsSelectedListener {

    private val viewModel: GroupChatViewModel by lazy {
        ViewModelProvider(this, GroupChatViewModelFactory(requireContext())).get(GroupChatViewModel::class.java)
    }
    private lateinit var groupId: String
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
            appDirectoryFileRef,
            initGlide(R.drawable.ic_user,R.drawable.ic_user)!!,
            this
        )
    }

    private val currentUserUid: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private val groupMembersRecyclerAdapter: GroupMembersRecyclerAdapter by lazy {
        GroupMembersRecyclerAdapter(
            initGlide(R.drawable.ic_user,R.drawable.ic_user)!!,
            this
        )
    }

    private val appDirectoryFileRef: File by lazy {
        Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_group_details, inflater, container)
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
            .chatGroupDetails
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> {
                        group_chat_main.gone()
                        group_chat_error.gone()
                        group_chat_progress_bar.visible()
                    }
                    is Lce.Content -> {
                        group_chat_progress_bar.gone()
                        group_chat_error.gone()
                        group_chat_main.visible()

                        showGroupDetails(it.content)
                    }
                    is Lce.Error -> {
                        group_chat_progress_bar.gone()
                        group_chat_main.gone()

                        group_chat_error.visible()
                        group_chat_error.text = it.error
                    }
                }
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
                    Lse.Loading -> UtilMethods.showLoading(requireContext())
                    Lse.Success -> UtilMethods.hideLoading()
                    is Lse.Error -> {
                        UtilMethods.hideLoading()

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
        groupMembersRecyclerAdapter.setData(content.groupMembers)

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

        if(isUserGroupManager){
            if (content.groupDeactivated) {
                add_giger_layout.isVisible = false
                deactivate_group_btn.isVisible = true
                deactivate_group_btn.text = "Activate Group"
                group_deactivated_container.visible()
            } else{
                deactivate_group_btn.text = "Deactivate Group"
                add_giger_layout.isVisible = true
                deactivate_group_btn.isVisible = true
                group_deactivated_container.gone()
            }
        } else{
            add_giger_layout.isVisible = false
            deactivate_group_btn.isVisible = false
        }

        if (content.groupDeactivated) {
            group_deactivated_container.visible()
        } else{
            group_deactivated_container.gone()
        }
    }

    private fun showDownloadingDialog() {
        UtilMethods.showLoading(requireContext())
    }

    private fun openDocument(file: File) {
        if (file.exists()) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    ), "application/pdf"
                )
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(this)
                } catch (e: Exception) {
                    showErrorDialog("Unable to open")
                }
            }
        } else {
            showErrorDialog("file_doesnt_exist")
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

        search_textview.textChanged {
            groupMembersRecyclerAdapter.filter.filter(it)
        }

        back_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        deactivate_group_btn.setOnClickListener {
            viewModel.deactivateOrActivateGroup()
        }

        media_forward_iv.setOnClickListener {
            navigate(
                R.id.groupMediaListFragment, bundleOf(
                    GroupMediaListFragment.INTENT_EXTRA_GROUP_ID to groupId
                )
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
                        showToast("Please enter a valid group name")
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

                val bundle = Bundle()
                bundle.putString(AppConstants.IMAGE_URL, it.imageUrl)
                bundle.putString(AppConstants.CONTACT_NAME, it.name)
                bundle.putString("chatHeaderId", "")
                bundle.putString("forUserId", currentUserUid)
                bundle.putString("otherUserId", it.uid)
                navigate(R.id.chatScreenFragment, bundle)
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

    private var contactLongPressed : ContactModel? = null
    override fun onGroupMemberItemLongPressed(view: View, position: Int, contact: ContactModel) {
        contactLongPressed = contact
        val popUp = PopupMenu(requireContext(), view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat_group)
        popUp.show()
    }

    override fun onChatIconClicked(position: Int, contact: ContactModel) {
        val bundle = Bundle()
//        bundle.putString(AppConstants.IMAGE_URL, contact.imageUrl)
//        bundle.putString(AppConstants.CONTACT_NAME, contact.name)

        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_IMAGE, contact.imageUrl)
        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_NAME,  contact.name)

        bundle.putString(ChatFragment.INTENT_EXTRA_CHAT_HEADER_ID, "")
        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_ID, contact.uid)
        navigate(R.id.chatScreenFragment, bundle)
    }

    override fun onContactsSelected(contacts: List<ContactModel>) {
        viewModel.addUsersGroup(contacts)
    }

    companion object {
        const val TAG = "GroupChatFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }

}