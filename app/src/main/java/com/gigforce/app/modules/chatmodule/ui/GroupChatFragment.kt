package com.gigforce.app.modules.chatmodule.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.gigforce.app.modules.chatmodule.models.GroupChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType
import com.gigforce.app.modules.chatmodule.models.VideoInfo
import com.gigforce.app.modules.chatmodule.ui.adapters.GroupChatRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnGroupChatMessageClickListener
import com.gigforce.app.modules.chatmodule.viewModels.GroupChatViewModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.gigforce.app.modules.chatmodule.viewModels.factories.GroupChatViewModelFactory
import kotlinx.android.synthetic.main.fragment_group_chat.*
import kotlinx.android.synthetic.main.fragment_group_chat_main.*
import java.io.File


class GroupChatFragment : BaseFragment(),
    PopupMenu.OnMenuItemClickListener,
    OnGroupChatMessageClickListener {

    private val viewModel: GroupChatViewModel by lazy {
        ViewModelProvider(this, GroupChatViewModelFactory(requireContext())).get(GroupChatViewModel::class.java)
    }

    private val appDirectoryFileRef: File by lazy {
        Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!
    }

    private val imagesDirectoryFileRef: File by lazy {
        if (!appDirectoryFileRef.exists()) {
            appDirectoryFileRef.mkdirs()
        }

        File(appDirectoryFileRef, ChatConstants.DIRECTORY_IMAGES)
    }

    private val videosDirectoryFileRef: File by lazy {
        if (!appDirectoryFileRef.exists()) {
            appDirectoryFileRef.mkdirs()
        }

        File(appDirectoryFileRef, ChatConstants.DIRECTORY_VIDEOS)
    }

    private val documentDirectoryFileRef: File by lazy {
        if (!appDirectoryFileRef.exists()) {
            appDirectoryFileRef.mkdirs()
        }

        File(appDirectoryFileRef, ChatConstants.DIRECTORY_DOCUMENTS)
    }

    private val uid: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private var selectedOperation = -1

    private val mAdapter: GroupChatRecyclerAdapter by lazy {
        GroupChatRecyclerAdapter(
            requireContext(),
            appDirectoryFileRef,
            initGlide(R.drawable.ic_user,R.drawable.ic_user)!!,
            this
        )
    }

    private lateinit var groupId: String

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GROUP_ID, groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_group_chat, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        init()
        subscribeViewModel()
        manageNewMessageToContact()
        manageBackIcon()
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
        val layoutManager = LinearLayoutManager(requireContext())

       layoutManager.stackFromEnd = true
//        layoutManager.reverseLayout = true
        rv_chats.layoutManager = layoutManager

        rv_chats.addItemDecoration(VerticalItemDecorator(30))
        // rv_chats.setHasFixedSize(true)
        rv_chats.adapter = mAdapter
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

        viewModel
            .sendingMessage
            .observe(viewLifecycleOwner, Observer {
                mAdapter.addItem(it)
                rv_chats.smoothScrollToPosition(mAdapter.itemCount - 1)
            })

        viewModel
            .groupMessages
            .observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    val msgs = it.map {
                        GroupChatMessage.fromMessage(it)
                    }

                    mAdapter.updateChatMessages(msgs)

                    if (mAdapter.itemCount != 0) {
                        rv_chats.smoothScrollToPosition(mAdapter.itemCount - 1)
                        viewModel.setMessagesUnseenCountToZero()
                    }
                }
            })

        viewModel.chatAttachmentDownloadState.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            when (it) {
                is DownloadStarted -> {
                    mAdapter.setItemAsDownloading(it.index)
                }
                is DownloadCompleted -> {
                    mAdapter.notifyItemChanged(it.index)
                }
                is ErrorWhileDownloadingAttachment -> {
                    mAdapter.setItemAsNotDownloading(it.index)
                }
            }
        })

        viewModel.setGroupId(groupId)
        viewModel.startWatchingGroupDetails()
        viewModel.startListeningForGroupMessages()
    }

    private fun showGroupDetails(content: ChatGroup) {

        val req = initGlide(R.drawable.ic_group,R.drawable.ic_group)

        if (content.groupAvatar.isNotBlank()) {
            val uri = Uri.parse(content.groupAvatar)
            req!!.load(uri).into(civ_personImage)
        } else {
            req!!.load(R.drawable.ic_group).into(civ_personImage)
        }

        tv_nameValueInChat.text = content.name

        val userRemovedFromGroup = content.groupMembers.find {
            it.uid == uid
        } == null

        if (content.groupDeactivated) {
            group_deactivated_container.visible()
            group_blocked_label.text = "This group is deactivated"
            container_footer.gone()
        } else if (userRemovedFromGroup) {
            group_deactivated_container.visible()
            group_blocked_label.text = "You have been removed from this group"
            container_footer.gone()
        } else {
            group_deactivated_container.gone()
            container_footer.visible()
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
        iv_verticalDots.setOnClickListener {
            manageMenu(it)
        }

        iv_greyPlus.setOnClickListener {
            val popUpMenu = PopupMenu(requireContext(), it)
            popUpMenu.setOnMenuItemClickListener(this)
            popUpMenu.inflate(R.menu.menu_chat_bottom)

            try {
                val popUp = PopupMenu::class.java.getDeclaredField("mPopup")
                popUp.isAccessible = true
                val menu = popUp.get(popUpMenu)
                menu.javaClass.getDeclaredMethod("setForceShowIcon" ,Boolean::class.java)
                    .invoke(menu,true)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                popUpMenu.show()
            }
        }

        tv_view_details.setOnClickListener {
            navigate(
                R.id.groupDetailsFragment, bundleOf(
                    GroupDetailsFragment.INTENT_EXTRA_GROUP_ID to groupId
                )
            )
        }
    }

    private fun manageMenu(view: View) {
        val popUp = PopupMenu(activity?.applicationContext, view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat)
        popUp.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_block -> {
            showToast("Block")
            true
        }
        R.id.action_report -> {
            showToast("Report")
            true
        }
        R.id.action_document -> {
            startPickingDocument()
            true
        }
        R.id.action_pick_image -> {
            if (isStoragePermissionGranted())
                pickImage()
            else {
                selectedOperation = ChatConstants.OPERATION_PICK_IMAGE
                askForStoragePermission()
            }
            true
        }
        R.id.action_video -> {
            pickVideo()
            true
        }
        else -> {
            false
        }
    }

    private fun pickVideo() = Intent(Intent.ACTION_GET_CONTENT).apply {

        if (!videosDirectoryFileRef.exists())
            videosDirectoryFileRef.mkdirs()

        type = "video/*"
        startActivityForResult(this, REQUEST_PICK_VIDEO)
    }

    private fun pickImage() {

        if (!imagesDirectoryFileRef.exists())
            imagesDirectoryFileRef.mkdirs()

        val newFileName = "$uid-${DateHelper.getFullDateTimeStamp()}.png"
        val imagefile = File(imagesDirectoryFileRef, newFileName)

        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_OUTPUT_FILE, imagefile)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(
            photoCropIntent,
            REQUEST_PICK_IMAGE
        )
    }

    private fun startPickingDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

        if (!documentDirectoryFileRef.exists())
            documentDirectoryFileRef.mkdirs()

        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(DOC, DOCX, XLS, PDF))
        startActivityForResult(this, REQUEST_PICK_DOCUMENT)
    }


    private fun manageNewMessageToContact() {
        iv_sendMessage.setOnClickListener {
            if (validateNewMessageTask()) {
                val message = et_typedMessageValue.text.toString()
                viewModel.sendNewText(message)
                et_typedMessageValue.setText("")
            }
        }
    }

    private fun validateNewMessageTask(): Boolean {
        if (et_typedMessageValue.text.toString().isBlank()) {
            return false
        }
        return true
    }

    private fun manageBackIcon() {
        iv_backArrowInChat.setOnClickListener {

            hideSoftKeyboard()
            activity?.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PICK_DOCUMENT -> if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data?.data ?: return
                val uriString = uri.toString()
                val myFile = File(uriString)

                var displayName: String? = getDisplayName(uriString, uri, myFile)
                viewModel.sendNewDocumentMessage("", displayName!!, uri)
                Log.d(TAG, displayName + "")
                Log.d(TAG, uriString)
            }
            REQUEST_PICK_IMAGE -> {
                val clickedImageUri: Uri =
                    data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI) ?: return
                viewModel.sendNewImageMessage(
                    text = "",
                    uri = clickedImageUri
                )
            }
            REQUEST_PICK_VIDEO -> {
                val uri = data?.data ?: return

                val uriString = uri.toString()
                val myFile = File(uriString)

                val videoInfo = getVideoInfo(uriString, uri, myFile)
                viewModel.sendNewVideoMessage(
                    requireContext(),
                    "",
                    videosDirectoryFileRef,
                    videoInfo,
                    uri
                )
            }
        }
    }

    private fun getDisplayName(
        uriString: String,
        uri: Uri,
        myFile: File
    ): String? {
        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {

                cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        } else if (uriString.startsWith("file://")) {
            return myFile.name
        }
        return ""
    }

    override fun chatMessageClicked(
        messageType: MessageType,
        position: Int,
        message: GroupChatMessage,
        fileDownloaded: Boolean,
        downloadedFile: File?
    ) {

        when (messageType) {
            MessageType.TEXT_WITH_IMAGE -> {

                if (fileDownloaded) {
                    ViewFullScreenImageDialogFragment.showImage(
                        childFragmentManager,
                        downloadedFile!!.toUri()
                    )
                } else {
                    if (message.toMessage().attachmentPath != null) {
                        viewModel.downloadAndSaveFile(
                            appDirectoryFileRef,
                            position,
                            message.toMessage()
                        )
                    }
                }
            }
            MessageType.TEXT_WITH_VIDEO -> {

                if (fileDownloaded) {
                    ViewFullScreenVideoDialogFragment.launch(
                        childFragmentManager,
                        downloadedFile!!.toUri()
                    )
                } else {
                    if (message.toMessage().attachmentPath != null) {
                        viewModel.downloadAndSaveFile(
                            appDirectoryFileRef,
                            position,
                            message.toMessage()
                        )
                    } else {

                    }
                }
            }
            MessageType.TEXT_WITH_DOCUMENT -> {

                if (fileDownloaded) {
                    openDocument(downloadedFile!!)
                } else {
                    if (message.toMessage().attachmentPath != null) {
                        viewModel.downloadAndSaveFile(
                            appDirectoryFileRef,
                            position,
                            message.toMessage()
                        )
                    } else {

                    }
                }
            }
            MessageType.NOT_SUPPORTED -> TODO()
            MessageType.DATE -> TODO()
            MessageType.TEXT -> TODO()
            MessageType.TEXT_WITH_LOCATION -> TODO()
            MessageType.TEXT_WITH_CONTACT -> TODO()
            MessageType.TEXT_WITH_AUDIO -> TODO()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForStoragePermission() {
        Log.v(ChatFragment.TAG, "Permission Required. Requesting Permission")
        requestPermissions(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ),
            REQUEST_STORAGE_PERMISSION
        )
    }

    override fun onBackPressed(): Boolean {
        findNavController().popBackStack(R.id.contactScreenFragment, false)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }

            if (allPermsGranted) {
                if (selectedOperation == ChatConstants.OPERATION_PICK_IMAGE) {
                    pickImage()
                    selectedOperation = -1
                } else if (selectedOperation == ChatConstants.OPERATION_PICK_VIDEO) {
                    pickVideo()
                    selectedOperation = -1
                } else if (selectedOperation == ChatConstants.OPERATION_PICK_DOCUMENT) {
                    startPickingDocument()
                    selectedOperation = -1
                }
            } else
                showToast("Please grant storage permission, to pick files")
        }
    }

    private fun getVideoInfo(
        uriString: String,
        uri: Uri,
        myFile: File
    ): VideoInfo {
        var fileName = ""
        var fileSize = 0L

        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val sizeInString = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))

                    fileSize = try {
                        sizeInString.toLong()
                    } catch (e: Exception) {
                        0L
                    }
                }
            } finally {
                cursor?.close()
            }
        } else if (uriString.startsWith("file://")) {
            fileName = myFile.name
            fileSize = myFile.length()
        }

        val videoLength = try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), uri)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLong() ?: 0L
        } catch (e: Exception) {
            Log.e("ChatGroupRepo", "Error while fetching video length", e)
            0L
        }

        val mMMR = MediaMetadataRetriever()
        mMMR.setDataSource(requireContext(), uri)
        val thumbnail: Bitmap? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                mMMR.getScaledFrameAtTime(
                    -1,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    196,
                    196
                )
            } else {
                try {
                    val bigThumbnail = mMMR.frameAtTime
                    val smallThumbnail = ThumbnailUtils.extractThumbnail(bigThumbnail, 196, 196)

                    if (!bigThumbnail.isRecycled)
                        bigThumbnail.recycle()

                    smallThumbnail
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        mMMR.release()

        return VideoInfo(
            name = fileName,
            duration = videoLength,
            size = fileSize,
            thumbnail = thumbnail
        )
    }


    companion object {
        private const val TAG = "ChatFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"

        private const val REQUEST_PICK_DOCUMENT = 102
        private const val REQUEST_PICK_IMAGE = 103
        private const val REQUEST_PICK_VIDEO = 104
        private const val REQUEST_STORAGE_PERMISSION = 105

        private const val DOC = "application/msword"
        private const val DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        private const val IMAGE = "image/*"
        private const val AUDIO = "audio/*"
        private const val TEXT = "text/*"
        private const val PDF = "application/pdf"
        private const val XLS = "application/vnd.ms-excel"
    }
}
