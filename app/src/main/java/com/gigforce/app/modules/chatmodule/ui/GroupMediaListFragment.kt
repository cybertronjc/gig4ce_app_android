package com.gigforce.app.modules.chatmodule.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.ChatConstants
import com.gigforce.app.modules.chatmodule.DownloadCompleted
import com.gigforce.app.modules.chatmodule.DownloadStarted
import com.gigforce.app.modules.chatmodule.ErrorWhileDownloadingAttachment
import com.gigforce.app.modules.chatmodule.models.ChatGroup
import com.gigforce.app.modules.chatmodule.models.GroupMedia
import com.gigforce.app.modules.chatmodule.ui.adapters.GroupMediaRecyclerAdapter
import com.gigforce.app.modules.chatmodule.viewModels.GroupChatViewModel
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.gigforce.app.utils.ViewFullScreenVideoDialogFragment
import com.vinners.cmi.ui.activity.GroupChatViewModelFactory
import kotlinx.android.synthetic.main.fragment_chat_group_media_list.*
import kotlinx.android.synthetic.main.fragment_chat_group_media_list_main.*
import java.io.File


class GroupMediaListFragment : BaseFragment(),
    GroupMediaRecyclerAdapter.OnGroupMediaClickListener {

    private val viewModel: GroupChatViewModel by lazy {
        ViewModelProvider(this, GroupChatViewModelFactory(requireContext())).get(GroupChatViewModel::class.java)
    }
    private lateinit var groupId: String
    private val groupMediaRecyclerAdapter: GroupMediaRecyclerAdapter by lazy {
        GroupMediaRecyclerAdapter(
            appDirectoryFileRef,
            initGlide()!!,
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
        return inflateView(R.layout.fragment_chat_group_media_list, inflater, container)
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
            .chatGroupDetails
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> {
                        group_chat_media_main.gone()
                        group_media_list_error.gone()
                        group_media_list_progress_bar.visible()
                    }
                    is Lce.Content -> {
                        group_media_list_progress_bar.gone()
                        group_media_list_error.gone()
                        group_chat_media_main.visible()

                        showGroupDetails(it.content)
                    }
                    is Lce.Error -> {
                        group_media_list_progress_bar.gone()
                        group_chat_media_main.gone()

                        group_media_list_error.visible()
                        group_media_list_error.text = it.error
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

        viewModel.setGroupId(groupId)
        viewModel.startWatchingGroupDetails()
    }

    private fun showGroupDetails(content: ChatGroup) {
        groupMediaRecyclerAdapter.setData(content.groupMedia)
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

    companion object {
        const val TAG = "GroupMediaListFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }
}