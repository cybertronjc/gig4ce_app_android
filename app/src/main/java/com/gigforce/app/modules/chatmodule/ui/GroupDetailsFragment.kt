package com.gigforce.app.modules.chatmodule.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.MessageType
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnChatMessageClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ChatMessagesViewModel
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class GroupDetailsFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    OnChatMessageClickListener {

    private val viewModel: ChatMessagesViewModel by activityViewModels<ChatMessagesViewModel>()

    private lateinit var mAdapter: ChatRecyclerAdapter
    private var imageUrl: String? = null
    private lateinit var username: String
    private lateinit var forUserId: String
    private lateinit var chatHeaderId: String
    private lateinit var otherUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getString(AppConstants.IMAGE_URL)
            username = it.getSerializable(AppConstants.CONTACT_NAME).toString()
            chatHeaderId = it.getString("chatHeaderId") ?: ""
            forUserId = it.getSerializable("forUserId").toString()
            otherUserId = it.getSerializable("otherUserId").toString()

            viewModel.headerId = chatHeaderId
            viewModel.otherUserName = username
            viewModel.otherUserProfilePicture = imageUrl
            viewModel.forUserId = forUserId
            viewModel.otherUserId = otherUserId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_chat_screen, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        manageTime()
        subscribeViewModel()
        manageNewMessageToContact()
        manageBackIcon()
    }


    private fun init() {
        mAdapter = ChatRecyclerAdapter(requireContext(), initGlide()!!, this)
        initIntent()
        initListeners()
        initRecycler()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(activity?.applicationContext)
        layoutManager.reverseLayout = true
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

                    //TODO improve UX here
                    rv_chats.smoothScrollToPosition(0)
                }
            })
        viewModel.startListeningForNewMessages()

        viewModel.downloadChatAttachment
            .observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                when (it) {
                    Lce.Loading -> {
                        showDownloadingDialog()
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        openDocument(it.content)
                    }
                    is Lce.Error -> {
                        UtilMethods.hideLoading()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Alert")
                            .setMessage(it.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                }
            })
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

    private fun initIntent() {
        val req = initGlide()
        val uri = Uri.parse(imageUrl)
        req!!.load(uri).into(civ_personImage)
        tv_nameValueInChat.text = username
    }

    private fun initListeners() {
        iv_verticalDots.setOnClickListener {
            manageMenu(it)
        }

        iv_greyPlus.setOnClickListener {
            val popUp = PopupMenu(activity?.applicationContext, it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_bottom)
            popUp.show()
        }
    }

    private fun manageMenu(view: View) {
        val popUp = PopupMenu(activity?.applicationContext, view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat)
        popUp.show()
    }

    private fun manageTime(): LocalDateTime {
        var current: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formatted = current.format(formatter)

        println("Current Date and Time is: $formatted")
        tv_lastSeenValue.text = "last seen at $formatted"
        return current
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
            pickImage()
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
        type = "video/*"
        startActivityForResult(this, REQUEST_PICK_VIDEO)
    }

    private fun pickImage() {
        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.PURPOSE_VERIFICATION
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
        startActivityForResult(
            photoCropIntent,
            REQUEST_PICK_IMAGE
        )
    }

    private fun startPickingDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
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
        message: ChatMessage
    ) = when (messageType) {
        MessageType.DATE -> TODO()
        MessageType.TEXT -> TODO()
        MessageType.TEXT_WITH_IMAGE -> {

            if (message.toMessage().attachmentPath != null) {
                ViewFullScreenImageDialogFragment.showImage(
                    childFragmentManager,
                    Uri.parse(message.toMessage().attachmentPath)
                )
            } else {
                //File Not available
            }
        }
        MessageType.TEXT_WITH_VIDEO -> {
            if (message.toMessage().attachmentPath != null) {
                ViewFullScreenVideoDialogFragment.launch(
                    childFragmentManager,
                    Uri.parse(message.toMessage().attachmentPath)
                )
            } else {
                //File Not available
            }
        }
        MessageType.TEXT_WITH_LOCATION -> TODO()
        MessageType.TEXT_WITH_CONTACT -> TODO()
        MessageType.TEXT_WITH_AUDIO -> TODO()
        MessageType.TEXT_WITH_DOCUMENT -> {
            if (message.toMessage().attachmentPath != null) {
                downloadAndShowAttachment(message.toMessage().attachmentPath!!)
            } else {
                //File Not available
            }
        }
        MessageType.NOT_SUPPORTED -> TODO()
    }

    private fun downloadAndShowAttachment(filePath: String) {
        viewModel.downloadAttachment(filePath, requireContext().filesDir)
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

                var displayName: String? = getDisplayName(uriString, uri, myFile)
                viewModel.sendNewVideoMessage(requireContext(), "", displayName!!, uri)
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

    companion object {
        const val TAG = "ChatFragment"

        const val REQUEST_PICK_DOCUMENT = 102
        const val REQUEST_PICK_IMAGE = 103
        const val REQUEST_PICK_VIDEO = 104

        const val DOC = "application/msword"
        const val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val IMAGE = "image/*"
        const val AUDIO = "audio/*"
        const val TEXT = "text/*"
        const val PDF = "application/pdf"
        const val XLS = "application/vnd.ms-excel"
    }
}
