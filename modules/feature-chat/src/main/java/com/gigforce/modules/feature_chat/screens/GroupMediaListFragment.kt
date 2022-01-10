package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ViewFullScreenImageDialogFragment
import com.gigforce.common_ui.ViewFullScreenVideoDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.*
import com.gigforce.modules.feature_chat.screens.adapters.GroupMediaRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat_group_media_list.*
import kotlinx.android.synthetic.main.fragment_chat_group_media_list_main.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class GroupMediaListFragment2 : Fragment(),
    GroupMediaRecyclerAdapter.OnGroupMediaClickListener {

    private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var groupId: String

    @Inject
    lateinit var chatFileManager : ChatFileManager

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }


    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
            requireContext(),
            chatFileManager.gigforceDirectory,
            Glide.with(requireContext()),
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_group_media_list, container, false)
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
        val layoutManager = GridLayoutManager(requireContext(),3)
        media_recyclerview.layoutManager = layoutManager
        media_recyclerview.adapter = groupMediaRecyclerAdapter
    }

    private fun subscribeViewModel() {

        viewModel
            .groupInfo
            .observe(viewLifecycleOwner, Observer {

                group_media_list_progress_bar.gone()
                group_media_list_error.gone()
                group_chat_media_main.visible()
                showGroupDetails(it)

//                when (it) {
//                    Lce.Loading -> {
//                        group_chat_media_main.gone()
//                        group_media_list_error.gone()
//                        group_media_list_progress_bar.visible()
//                    }
//                    is Lce.Content -> {
//                        group_media_list_progress_bar.gone()
//                        group_media_list_error.gone()
//                        group_chat_media_main.visible()
//
//
//                    }
//                    is Lce.Error -> {
//                        group_media_list_progress_bar.gone()
//                        group_chat_media_main.gone()
//
//                        group_media_list_error.visible()
//                        group_media_list_error.text = it.error
//                    }
//                }
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

        if (groupId.isEmpty()) {
            CrashlyticsLogger.e(TAG, "getting args from arguments", Exception("$groupId <-- String passed as groupId"))
            throw IllegalArgumentException("$groupId <-- String passed as groupId")
        }

        viewModel.setGroupId(groupId)
        viewModel.startWatchingGroupDetails()
    }

    private fun showGroupDetails(content: ChatGroup) {
        groupMediaRecyclerAdapter.setData(content.groupMedia)
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

                Toast.makeText(
                    requireContext(),
                    "Unable to open document",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showErrorDialog(error: String) {

    }

    private fun initListeners() {

        back_arrow.setOnClickListener {
            activity?.onBackPressed()
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
            viewModel.downloadAndSaveFile(
                chatFileManager.gigforceDirectory,
                position,
                media
            )
        }
    }

    private fun openAudioPlayerBottomSheet(file: File) {
        navigation.navigateTo(
            "chats/audioPlayer", bundleOf(
                AudioPlayerBottomSheetFragment.INTENT_EXTRA_URI to file.path!!
            )
        )
    }

    companion object {
        const val TAG = "GroupMediaListFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }
}