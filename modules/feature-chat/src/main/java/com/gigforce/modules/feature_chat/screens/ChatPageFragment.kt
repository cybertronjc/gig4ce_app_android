package com.gigforce.modules.feature_chat.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_ui.rv.VerticalItemDecorator
import com.gigforce.core.PermissionUtils
import com.gigforce.core.StringConstants
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatLocalDirectoryReferenceManager
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.VideoInfo
import com.gigforce.modules.feature_chat.screens.vm.ChatPage2ViewModel
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.ui.ChatFooter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.sql.Timestamp
import java.util.*

class ChatPageFragment : Fragment(),
        PopupMenu.OnMenuItemClickListener,
        ImageCropCallback {

    //Views
    private lateinit var chatRecyclerView: CoreRecyclerView
    private lateinit var chatFooter: ChatFooter
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarOverflowBtn: ImageView
    private lateinit var toolbarUserImageIV: ImageView
    private lateinit var lastSeenTV: TextView
    private lateinit var userBlockedOrRemovedLayout : TextView

    private val viewModel: ChatPageViewModel by viewModels()
    private val viewModel2: ChatPage2ViewModel by viewModels()

    private val cameraAndGalleryIntegrator: CameraAndGalleryIntegrator by lazy {
        CameraAndGalleryIntegrator(this)
    }

    private val chatLocalDirectoryReferenceManager: ChatLocalDirectoryReferenceManager by lazy {
        ChatLocalDirectoryReferenceManager()
    }

    private val imageCropOptions: ImageCropOptions
        get() {

            if (!chatLocalDirectoryReferenceManager.imagesDirectoryRef.exists()) {
                chatLocalDirectoryReferenceManager.imagesDirectoryRef.mkdirs()
            }

            val newFileName = "Chat-${DateHelper.getFullDateTimeStamp()}.png"
            val imageFile = File(chatLocalDirectoryReferenceManager.imagesDirectoryRef, newFileName)

            return ImageCropOptions
                    .Builder()
                    .shouldOpenImageCrop(true)
                    .setShouldEnableFaceDetector(false)
                    .setOutputFileUri(imageFile.toUri())
                    .build()
        }

    //Other User Info
    private lateinit var chatHeaderId: String
    private lateinit var otherUserId: String
    private lateinit var username: String
    private lateinit var mobileNumber: String
    private lateinit var imageUrl: String
    private var fromClientActivation: Boolean = false

    private var selectedOperation = -1

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)

        findViews(view)
        init()
        subscribeViewModel()
        manageNewMessageToContact()
    }

    private fun findViews(view: View) {
        toolbarTitle = view.findViewById(R.id.tv_nameValueInChat)
        toolbarOverflowBtn = view.findViewById(R.id.iv_verticalDots)

        chatFooter = view.findViewById(R.id.chat_footer)
        chatRecyclerView = view.findViewById(R.id.rv_chat_messages)
        chatRecyclerView.addItemDecoration(VerticalItemDecorator(20))


        toolbarUserImageIV = view.findViewById(R.id.user_image_iv)

        lastSeenTV = view.findViewById(R.id.tv_lastSeenValue)
        userBlockedOrRemovedLayout = view.findViewById(R.id.contact_blocked_label)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            fromClientActivation = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            imageUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            username = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            otherUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)!!
            mobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
        }

        savedInstanceState?.let {
            fromClientActivation = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            imageUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            username = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            otherUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)!!
            mobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""

        }

        viewModel.setRequiredDataAndStartListeningToMessages(
                otherUserId = otherUserId,
                headerId = chatHeaderId,
                otherUserName = username,
                otherUserProfilePicture = imageUrl
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            /*
            putString(INTENT_EXTRA_OTHER_USER_IMAGE, imageUrl)
            putString(INTENT_EXTRA_OTHER_USER_NAME, username)
            putString(INTENT_EXTRA_CHAT_HEADER_ID, chatHeaderId)
            putString(INTENT_EXTRA_FOR_USER_ID, forUserId)
            putString(INTENT_EXTRA_OTHER_USER_ID, otherUserId)
            putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, fromClientActivation)
            putString(StringConstants.MOBILE_NUMBER.value, mobileNumber)
             */
        }
    }

    private fun init() {
        initIntent()
        initListeners()
    }

    private fun subscribeViewModel() {

        viewModel.otherUserInfo
                .observe(viewLifecycleOwner, Observer {

                    if (it.name.isNullOrBlank()) {
                        toolbarTitle.text = it.mobile
                    } else {
                        toolbarTitle.text = it.name
                    }

                    if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                        val thumbnailRef =
                                firebaseStorage.reference.child(it.imageThumbnailPathInStorage!!)
                        Glide.with(requireContext())
                                .load(thumbnailRef)
                                .placeholder(R.drawable.ic_user)
                                .into(toolbarUserImageIV)
                    } else if (!it.imagePathInStorage.isNullOrBlank()) {

                        val thumbnailRef = firebaseStorage.reference.child(it.imagePathInStorage!!)
                        Glide.with(requireContext())
                                .load(thumbnailRef)
                                .placeholder(R.drawable.ic_user)
                                .into(toolbarUserImageIV)
                    } else {

                        Glide.with(requireContext())
                                .load(R.drawable.ic_user)
                                .placeholder(R.drawable.ic_user)
                                .into(toolbarUserImageIV)

                    }
                })

        viewModel.messages
                .observe(viewLifecycleOwner, Observer {
                    chatRecyclerView.collection = it
                    chatRecyclerView.smoothScrollToLastPosition()
                })

        viewModel.headerInfo
                .observe(viewLifecycleOwner, Observer {

                    if(it.isBlocked){
                        userBlockedOrRemovedLayout.visible()
                        userBlockedOrRemovedLayout.text = "You've blocked this contact"
                        chatFooter.gone()
                    } else {
                        userBlockedOrRemovedLayout.gone()
                        chatFooter.visible()
                    }

                    if (it.isOtherUserOnline) {
                        lastSeenTV.visible()
                        lastSeenTV.text = "Online"
                    } else {
                        if (it.lastUserStatusActivityAt != 0L) {
                            lastSeenTV.visible()

                            val timeStamp = Timestamp(it.lastUserStatusActivityAt)
                            val date = Date(timeStamp.time)
                            lastSeenTV.text = "Last Seen At: ${date.toDisplayText()}"
                        } else {
                            lastSeenTV.gone()
                            lastSeenTV.text = null
                        }
                    }
                })
    }

    private fun openDocument(file: File) {
        if (file.exists()) {
            Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(
//                    FileProvider.getUriForFile(
//                        requireContext(),
//                        BuildConfig.APPLICATION_ID + ".provider",
//                        file
//                    ), "application/pdf"
//                )
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
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Message")
                .setMessage(error)
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun initIntent() {
//        val req = initGlide(R.drawable.ic_user, R.drawable.ic_user)
//
//        if (imageUrl.isNullOrBlank()) {
//            req!!.load(R.drawable.ic_user).into(civ_personImage)
//        } else {
//            val uri = Uri.parse(imageUrl)
//            req!!.load(uri).into(civ_personImage)
//        }
//        toolbarTitle.text =
//            if (fromClientActivation) checkForContact(mobileNumber, username) else username
    }

    private fun initListeners() {
        toolbarOverflowBtn.setOnClickListener {
            manageMenu(it)
        }

        chatFooter.attachmentOptionButton.setOnClickListener {
            val popUpMenu = PopupMenu(requireContext(), it)
            popUpMenu.setOnMenuItemClickListener(this)
            popUpMenu.inflate(R.menu.menu_chats)

            try {
                val popUp = PopupMenu::class.java.getDeclaredField("mPopup")
                popUp.isAccessible = true
                val menu = popUp.get(popUpMenu)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                popUpMenu.show()
            }
        }
    }

    private fun manageMenu(view: View) {
        val popUp = PopupMenu(requireContext(), view)
        popUp.setOnMenuItemClickListener(this)
        popUp.inflate(R.menu.menu_chat_toolbar)
        popUp.menu.findItem(R.id.action_block).title =
                if (chatFooter.isVisible) "Block" else "UnBlock"
        popUp.show()
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
        R.id.action_location -> {
            startActivityForResult(
                    Intent(requireContext(), CaptureLocationActivity::class.java),
                    REQUEST_GET_LOCATION
            )
            true
        }
        R.id.action_pick_image -> {
            if (isStoragePermissionGranted()) {
                pickImage()
            } else {
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

    private fun pickVideo() {
        try {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
                startActivityForResult(this, REQUEST_PICK_VIDEO)
            }
        } catch (e: ActivityNotFoundException) {
            showErrorDialog("No App found to pick Video")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().apply {
                log("Unable to pick video")
                recordException(e)
            }
        }
    }

    private fun pickImage() {

//        if (!imagesDirectoryFileRef.exists())
//            imagesDirectoryFileRef.mkdirs()
//
//        val newFileName = "$uid-${DateHelper.getFullDateTimeStamp()}.png"
//        val imagefile = File(imagesDirectoryFileRef, newFileName)

//        val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
//        photoCropIntent.putExtra(
//            PhotoCrop.INTENT_EXTRA_PURPOSE,
//            PhotoCrop.PURPOSE_VERIFICATION
//        )
//        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
//        photoCropIntent.putExtra("folder", "verification")
//        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
//        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_OUTPUT_FILE, imagefile)
//        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "aadhar_card_front.jpg")
//        startActivityForResult(
//            photoCropIntent,
//            REQUEST_PICK_IMAGE
//        )
        cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
    }

    private fun pickDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(DOC, DOCX, XLS, PDF))
        startActivityForResult(this, REQUEST_PICK_DOCUMENT)
    }


    private fun manageNewMessageToContact() {
        chatFooter.btn_send.setOnClickListener {
            if (validateNewMessageTask()) {
                val message = chatFooter.et_message.text.toString()
                viewModel.sendNewText(message)
                chatFooter.et_message.setText("")
            }
        }
    }

    private fun validateNewMessageTask(): Boolean {

        if (chatFooter.et_message.text.toString().isBlank()) {
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.reqCodePerm
                && PermissionUtils.permissionsGrantedCheck(grantResults)
        ) {
            toolbarTitle.text = checkForContact(mobileNumber, username)
        }


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
//                    pickImage()
                    selectedOperation = -1
                } else if (selectedOperation == ChatConstants.OPERATION_PICK_VIDEO) {
//                    pickVideo()
                    selectedOperation = -1
                } else if (selectedOperation == ChatConstants.OPERATION_PICK_DOCUMENT) {
//                    pickDocument()
                    selectedOperation = -1
                }
            } else
                Toast.makeText(
                        requireContext(),
                        "Please grant storage permission, to pick files",
                        Toast.LENGTH_SHORT
                ).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionUtils.reqCodePerm -> toolbarTitle.text = checkForContact(mobileNumber, username)
            REQUEST_PICK_DOCUMENT -> if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data?.data ?: return
                val uriString = uri.toString()
                val myFile = File(uriString)

                val displayName: String? = getDisplayName(uriString, uri, myFile)


                viewModel.sendNewDocumentMessage(
                        requireContext(),
                        "",
                        displayName,
                        uri
                )

                Log.d(TAG, displayName + "")
                Log.d(TAG, uriString)
            }
            CameraAndGalleryIntegrator.REQUEST_CAPTURE_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_PICK_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_CROP -> {
                cameraAndGalleryIntegrator.parseResults(
                        requestCode,
                        resultCode,
                        data,
                        imageCropOptions,
                        this@ChatPageFragment
                )
            }
            REQUEST_PICK_VIDEO -> {
                val uri = data?.data ?: return

                val uriString = uri.toString()
                val myFile = File(uri.path)

                val videoInfo = getVideoInfo(uriString, uri, myFile)
                viewModel.sendNewVideoMessage(
                        requireContext(),
                        "",
                        videoInfo,
                        uri
                )
            }
            REQUEST_GET_LOCATION -> {
                val latitude = data!!.getDoubleExtra(CaptureLocationActivity.INTENT_EXTRA_LATITUDE, 0.0)
                val longitude = data.getDoubleExtra(CaptureLocationActivity.INTENT_EXTRA_LONGITUDE, 0.0)
                val address = data.getStringExtra(CaptureLocationActivity.INTENT_EXTRA_PHYSICAL_ADDRESS)
                        ?: ""
                val imageFile: File? = data.getSerializableExtra(CaptureLocationActivity.INTENT_EXTRA_MAP_IMAGE_FILE) as File?

                viewModel.sendLocationMessage(
                        latitude,
                        longitude,
                        address,
                        imageFile
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

                        if (!bigThumbnail!!.isRecycled)
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

    fun checkForContact(number: String?, name: String): String {
        var nameFromDB = name
        return if (number != null && number.length >= 10 && PermissionUtils.checkForPermissionFragment(
                        this,
                        PermissionUtils.reqCodePerm,
                        Manifest.permission.READ_CONTACTS
                )
        ) {
            val lookupUri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number)
            )
            val mPhoneNumberProjection = arrayOf<String>(
                    ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME
            )
            val cur: Cursor? = requireActivity().contentResolver.query(
                    lookupUri,
                    mPhoneNumberProjection,
                    null,
                    null,
                    null
            )
            cur.use { cur ->
                if (cur?.moveToFirst() == true) {
                    nameFromDB =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))

                }
            }
            nameFromDB
        } else {
            nameFromDB
        }
    } //


    companion object {
        const val TAG = "ChatFragment"

        const val INTENT_EXTRA_CHAT_HEADER_ID = "chat_header_id"
        const val INTENT_EXTRA_OTHER_USER_ID = "sender_id"
        const val INTENT_EXTRA_OTHER_USER_NAME = "sender_name"
        const val INTENT_EXTRA_OTHER_USER_IMAGE = "sender_profile"

        private const val REQUEST_PICK_DOCUMENT = 202
        private const val REQUEST_PICK_IMAGE = 203
        private const val REQUEST_PICK_VIDEO = 204
        private const val REQUEST_GET_LOCATION = 207
        private const val REQUEST_STORAGE_PERMISSION = 205

        private const val DOC = "application/msword"
        private const val DOCX =
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        private const val IMAGE = "image/*"
        private const val AUDIO = "audio/*"
        private const val TEXT = "text/*"
        private const val PDF = "application/pdf"
        private const val XLS = "application/vnd.ms-excel"
    }

    //
    //-----------------------------
    // Camera , Gallery and Image crop callbacks
    //-------------------------

    override fun errorWhileCapturingOrPickingImage(e: Exception) {
        showErrorDialog(e.message ?: "Unable to capture and click image")
        FirebaseCrashlytics.getInstance().apply {
            log("Unable to click or capture image")
            recordException(e)
        }
    }

    override fun imageResult(uri: Uri) {
        viewModel.sendNewImageMessage(
                text = "",
                uri = uri
        )
    }
}
