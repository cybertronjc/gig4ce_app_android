package com.gigforce.app.modules.chatmodule.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnChatMessageClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ChatMessagesViewModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.VerticalItemDecorator
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.gigforce.app.utils.ViewFullScreenVideoDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_chat_new_contact.*
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatFragment : BaseFragment(),
    PopupMenu.OnMenuItemClickListener,
    OnChatMessageClickListener {

    private val viewModel: ChatMessagesViewModel by viewModels()

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

    private val mAdapter: ChatRecyclerAdapter by lazy {
        ChatRecyclerAdapter(
            requireContext(),
            appDirectoryFileRef,
            initGlide()!!,
            this
        )
    }

    private var imageUrl: String? = null
    private lateinit var username: String
    private lateinit var forUserId: String
    private lateinit var chatHeaderId: String
    private lateinit var otherUserId: String

    private var selectedOperation = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_screen, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        init()
        manageTime()
        subscribeViewModel()
        manageNewMessageToContact()
        manageBackIcon()


    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            imageUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE)
            username = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            forUserId = it.getString(INTENT_EXTRA_FOR_USER_ID)!!
            otherUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)!!
        }

        savedInstanceState?.let {
            imageUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE)
            username = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            forUserId = it.getString(INTENT_EXTRA_FOR_USER_ID)!!
            otherUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)!!
        }

        viewModel.headerId = chatHeaderId
        viewModel.otherUserName = username
        viewModel.otherUserProfilePicture = imageUrl
        viewModel.forUserId = forUserId
        viewModel.otherUserId = otherUserId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(INTENT_EXTRA_OTHER_USER_IMAGE, imageUrl)
            putString(INTENT_EXTRA_OTHER_USER_NAME, username)
            putString(INTENT_EXTRA_CHAT_HEADER_ID, chatHeaderId)
            putString(INTENT_EXTRA_FOR_USER_ID, forUserId)
            putString(INTENT_EXTRA_OTHER_USER_ID, otherUserId)
        }
    }

    private fun init() {
        initIntent()
        initListeners()
        initRecycler()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true
        rv_chats.layoutManager = layoutManager

        rv_chats.addItemDecoration(VerticalItemDecorator(30))
        // rv_chats.setHasFixedSize(true)
        rv_chats.adapter = mAdapter
    }

    private fun subscribeViewModel() {

        viewModel
            .messages
            .observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    val msgs = it.map {
                        ChatMessage.fromMessage(it)
                    }
                    mAdapter.updateChatMessages(msgs)

                    if (mAdapter.itemCount != 0) {
                        rv_chats.smoothScrollToPosition(mAdapter.itemCount - 1)
                        viewModel.setMessagesUnseenCountToZero()
                    }
                }
            })

        viewModel.sendingMessage
            .observe(viewLifecycleOwner, Observer {
                mAdapter.addItem(it)
                rv_chats.smoothScrollToPosition(mAdapter.itemCount - 1)
            })

        viewModel.headerInfo
            .observe(viewLifecycleOwner, Observer {

                if (it.isBlocked) {
                    contact_blocked_container.visible()
                    container_footer.gone()
                } else {
                    contact_blocked_container.gone()
                    container_footer.visible()
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
        viewModel.startListeningForNewMessages()

        viewModel.chatAttachmentDownloadState
            .observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                when (it) {
                    is DownloadStarted -> {
                        mAdapter.setItemAsDownloading(it.index)
                    }
                    is DownloadCompleted -> {
                        mAdapter.setItemAsNotDownloading(it.index)
                    }
                    is ErrorWhileDownloadingAttachment -> {
                        mAdapter.setItemAsNotDownloading(it.index)

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to download attachment")
                            .setMessage(it.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                }
            })
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

    private fun initIntent() {
        val req = initGlide()

        if (imageUrl.isNullOrBlank()) {
            req!!.load(R.drawable.ic_user).into(civ_personImage)
        } else {
            val uri = Uri.parse(imageUrl)
            req!!.load(uri).into(civ_personImage)
        }
        tv_nameValueInChat.text = username
    }

    private fun initListeners() {
        iv_verticalDots.setOnClickListener {
            manageMenu(it)
        }

        iv_greyPlus.setOnClickListener {
            val popUp = PopupMenu(requireContext(), it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_bottom)
            popUp.show()
        }
    }

    private fun manageMenu(view: View) {
        val popUp = PopupMenu(requireContext(), view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat)
        popUp.menu.findItem(R.id.action_block).title =
            if (contact_blocked_container.isVisible) "Unblock" else "Block"
        popUp.show()
    }

    private fun manageTime(): LocalDateTime {
        var current: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formatted = current.format(formatter)

        println("Current Date and Time is: $formatted")
        // tv_lastSeenValue.text = "last seen at : -"
        return current
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_block -> {
            viewModel.blockOrUnBlockUser()
            true
        }
        R.id.action_report -> {
            ReportUserDialogFragment.launch(
                viewModel.headerId,
                viewModel.otherUserId,
                childFragmentManager
            )
            true
        }
        R.id.action_document -> {

            if (isStoragePermissionGranted())
                pickDocument()
            else {
                selectedOperation = ChatConstants.OPERATION_PICK_DOCUMENT
                askForStoragePermission()
            }
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
            if (isStoragePermissionGranted())
                pickVideo()
            else {
                selectedOperation = ChatConstants.OPERATION_PICK_VIDEO
                askForStoragePermission()
            }

            true
        }
        else -> {
            false
        }
    }

    private fun askForStoragePermission() {
        Log.v(TAG, "Permission Required. Requesting Permission")
        requestPermissions(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ),
            REQUEST_STORAGE_PERMISSION
        )
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

    private fun pickDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

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
                val msgTime = manageTime()
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
            activity?.onBackPressed()
        }
    }

    override fun chatMessageClicked(
        messageType: MessageType,
        position: Int,
        message: ChatMessage,
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
                    } else {

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
                    pickDocument()
                    selectedOperation = -1
                }
            } else
                showToast("Please grant storage permission, to pick files")
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

                val displayName: String? = getDisplayName(uriString, uri, myFile)

                if (!documentDirectoryFileRef.exists())
                    documentDirectoryFileRef.mkdirs()

                viewModel.sendNewDocumentMessage(
                    requireContext(),
                    "",
                    documentDirectoryFileRef,
                    displayName,
                    uri
                )

                Log.d(TAG, displayName + "")
                Log.d(TAG, uriString)
            }
            REQUEST_PICK_IMAGE -> {

                if (!imagesDirectoryFileRef.exists())
                    imagesDirectoryFileRef.mkdirs()

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

                val displayName: String? = getDisplayName(uriString, uri, myFile)
                viewModel.sendNewVideoMessage(
                    requireContext(),
                    videosDirectoryFileRef,
                    "",
                    displayName!!,
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

    companion object {
        const val TAG = "ChatFragment"

        const val INTENT_EXTRA_CHAT_HEADER_ID = "chatHeaderId"
        const val INTENT_EXTRA_FOR_USER_ID = "forUserId"
        const val INTENT_EXTRA_OTHER_USER_ID = "otherUserId"
        const val INTENT_EXTRA_OTHER_USER_NAME = "contactName"
        const val INTENT_EXTRA_OTHER_USER_IMAGE = "imageUrl"

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
