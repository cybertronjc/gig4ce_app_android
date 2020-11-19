package com.gigforce.app.modules.chatmodule.ui

import android.app.Activity
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
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.models.ChatMessage
import com.gigforce.app.modules.chatmodule.models.Message
import com.gigforce.app.modules.chatmodule.models.MessageType
import com.gigforce.app.modules.chatmodule.ui.adapters.ChatRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.OnChatMessageClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ChatMessagesViewModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.AddDrivingLicenseInfoFragment
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.VerticalItemDecorator
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_pan_card_info_main.*
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
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
        mAdapter = ChatRecyclerAdapter(requireContext(),initGlide()!!,this)
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

        viewModel.getChatMessagesLiveData()
                .observe(viewLifecycleOwner, Observer
                {
                    if (it != null) {
                        val msgs = it.map {
                            ChatMessage.fromMessage(it)
                        }

                        mAdapter.updateChatMessages(msgs)
                        rv_chats.smoothScrollToPosition(0)
                    }
                })
    }

    private fun initIntent() {
        val req = initGlide()
        val uri = Uri.parse("android.resource://com.gigforce.app/drawable/" + imageUrl)
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
        else -> {
            false
        }
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
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(DOC, DOCX, XLS,PDF))
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
    ) = when(messageType){
        MessageType.DATE -> TODO()
        MessageType.TEXT -> TODO()
        MessageType.TEXT_WITH_IMAGE -> {

            if(message.toMessage().attachmentPath != null){
                ViewFullScreenImageDialogFragment.showImage(childFragmentManager,Uri.parse(message.toMessage().attachmentPath))
            } else{
                //File Not available
            }
        }
        MessageType.TEXT_WITH_VIDEO -> TODO()
        MessageType.TEXT_WITH_LOCATION -> TODO()
        MessageType.TEXT_WITH_CONTACT -> TODO()
        MessageType.TEXT_WITH_AUDIO -> TODO()
        MessageType.TEXT_WITH_DOCUMENT -> TODO()
        MessageType.NOT_SUPPORTED -> TODO()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PICK_DOCUMENT -> if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data?.data ?: return
                val uriString = uri.toString()
                val myFile = File(uriString)

                var displayName: String? = null

                if (uriString.startsWith("content://")) {
                    var cursor: Cursor? = null
                    try {

                        cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        cursor?.close()
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.name
                }


                viewModel.sendNewDocumentMessage("",displayName!!,uri)

                Log.d(TAG, displayName +"")
                Log.d(TAG, uriString)
            }
            REQUEST_PICK_IMAGE -> {
                val clickedImageUri : Uri = data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI) ?: return
                viewModel.sendNewImageMessage(
                    text = "",
                    uri = clickedImageUri
                )
            }
        }
    }

    companion object {
        const val TAG = "ChatFragment"

        const val REQUEST_PICK_DOCUMENT = 102
        const val REQUEST_PICK_IMAGE = 103

        const val DOC = "application/msword"
        const val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val IMAGE = "image/*"
        const val AUDIO = "audio/*"
        const val TEXT = "text/*"
        const val PDF = "application/pdf"
        const val XLS = "application/vnd.ms-excel"
    }
}
