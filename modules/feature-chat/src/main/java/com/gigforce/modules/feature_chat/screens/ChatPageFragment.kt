package com.gigforce.modules.feature_chat.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
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
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.PermissionUtils
import com.gigforce.core.StringConstants
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatLocalDirectoryReferenceManager
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ChatGroup
import com.gigforce.modules.feature_chat.models.VideoInfo
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.gigforce.modules.feature_chat.ui.ChatFooter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_chat.*
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChatPageFragment : Fragment(),
        PopupMenu.OnMenuItemClickListener,
        ImageCropCallback {

    @Inject
    lateinit var navigation: IChatNavigation

    //Views
    private lateinit var chatRecyclerView: CoreRecyclerView
    private lateinit var chatFooter: ChatFooter

    private lateinit var toolBackBtn: View
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarOverflowBtn: ImageView
    private lateinit var toolbarUserImageIV: GigforceImageView
    private lateinit var lastSeenTV: TextView
    private lateinit var userBlockedOrRemovedLayout: TextView

    private val viewModel: ChatPageViewModel by viewModels()
    private val groupChatViewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
                this,
                GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }

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

    //-------------------------------------
    //One-to-one and group chat common info
    //-------------------------------------
    private var chatType: String = ChatConstants.CHAT_TYPE_USER
    private var chatHeaderOrGroupId: String? = null

    //-------------------------------------
    //Info specific to one to one chat
    //-------------------------------------
    private var receiverUserId: String? = null
    private var receiverName: String? = null
    private var receiverMobileNumber: String? = null
    private var receiverPhotoUrl: String? = null
    private var fromClientActivation: Boolean = false

    private var selectedOperation = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (this.requireContext().applicationContext as ChatModuleProvider)
                .provideChatModule()
                .inject(this)
        navigation.context = requireContext()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        validateIfRequiredDataIsAvailable()

        cancelAnyNotificationIfShown()
        findViews(view)
        init()
        manageNewMessageToContact()
        checkForChatTypeAndSubscribeToRespectiveViewModel()
    }

    private fun cancelAnyNotificationIfShown() {
        val mNotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.cancel(67)
    }

    private fun validateIfRequiredDataIsAvailable() {
        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            if (receiverUserId == null) {

                CrashlyticsLogger.e(
                        TAG,
                        "Checking for neccessary data at startup",
                        IllegalArgumentException("Chat type is one-one chat but receiverUserId was null")
                )
                throw IllegalArgumentException("Chat type is one-one chat but receiverUserId was null")
            }

        } else if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
            if (chatHeaderOrGroupId.isNullOrBlank()) {
                FirebaseCrashlytics.getInstance().apply {
                    recordException(IllegalArgumentException("Chat type is group chat but chatHeaderOrGroupId was null"))
                }

                throw IllegalArgumentException("ChatPageFragment : for chat-type group you will need to pass group id ")
            }
        }
    }

    private fun checkForChatTypeAndSubscribeToRespectiveViewModel() {

        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            viewModel.setRequiredDataAndStartListeningToMessages(
                    otherUserId = receiverUserId!!,
                    headerId = chatHeaderOrGroupId,
                    otherUserName = receiverName,
                    otherUserProfilePicture = receiverPhotoUrl
            )
            adjustUiAccToOneToOneChat()
            subscribeOneToOneViewModel()
        } else if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
            groupChatViewModel.setGroupId(chatHeaderOrGroupId!!)
            adjustUiAccToGroupChat()
            subscribeChatGroupViewModel()
        }
    }

    private fun adjustUiAccToOneToOneChat() {
        tv_lastSeenValue.visible()
        tv_lastSeenValue.text = "Offline"
    }

    private fun adjustUiAccToGroupChat() {
        toolbarOverflowBtn.gone()
        tv_lastSeenValue.visible()
        tv_lastSeenValue.text = "Tap to open details"

        tv_lastSeenValue.setOnClickListener {
            val groupId = chatHeaderOrGroupId ?: return@setOnClickListener

            if(chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.openGroupDetailsPage(
                        groupId
                )
            }
        }

        toolbarTitle.setOnClickListener {
            val groupId = chatHeaderOrGroupId ?: return@setOnClickListener

            if(chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.openGroupDetailsPage(
                        groupId
                )
            }
        }
    }

    private fun subscribeChatGroupViewModel() {
        groupChatViewModel
                .outputs
                .groupInfo
                .observe(viewLifecycleOwner, Observer {

                    showGroupDetails(it)
                })

        groupChatViewModel.outputs
                .groupInfo
                .observe(viewLifecycleOwner, Observer {


                    if (it.groupDeactivated) {
                        userBlockedOrRemovedLayout.visible()
                        userBlockedOrRemovedLayout.text = "This group is deactivated by admin"
                        chatFooter.gone()
                    } else {
                        userBlockedOrRemovedLayout.gone()
                        chatFooter.visible()
                    }
                })


        groupChatViewModel
                .outputs
                .messages
                .observe(viewLifecycleOwner, Observer {

                    chatRecyclerView.collection = it
                    chatRecyclerView.smoothScrollToLastPosition()
                })

        groupChatViewModel
                .inputs
                .getGroupInfoAndStartListeningToMessages()
    }

    private fun showGroupDetails(group: ChatGroup) {
        toolbarTitle.text = group.name

        if (group.groupAvatarThumbnail.isNotBlank()) {
            toolbarUserImageIV.loadImageFromFirebase(group.groupAvatarThumbnail,R.drawable.ic_group)
        } else if (group.groupAvatar.isNotBlank()) {
            toolbarUserImageIV.loadImageFromFirebase(group.groupAvatarThumbnail,R.drawable.ic_group)
        } else {
            toolbarUserImageIV.loadImage(R.drawable.ic_group)
        }
    }

    private fun findViews(view: View) {
        toolbarTitle = view.findViewById(R.id.tv_nameValueInChat)
        toolbarOverflowBtn = view.findViewById(R.id.iv_verticalDots)
        toolBackBtn = view.findViewById(R.id.iv_backArrowInChat)

        chatFooter = view.findViewById(R.id.chat_footer)

        chatRecyclerView = view.findViewById(R.id.rv_chat_messages)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager

        //chatRecyclerView.addItemDecoration(VerticalItemDecorator(5))

        toolbarUserImageIV = view.findViewById(R.id.user_image_iv)

        lastSeenTV = view.findViewById(R.id.tv_lastSeenValue)
        userBlockedOrRemovedLayout = view.findViewById(R.id.contact_blocked_label)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            chatType = it.getString(INTENT_EXTRA_CHAT_TYPE)
                    ?: throw IllegalArgumentException("please provide INTENT_EXTRA_CHAT_TYPE in intent extra")
            fromClientActivation = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            receiverPhotoUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            receiverName = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderOrGroupId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            receiverUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)
            receiverMobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""
        }

        savedInstanceState?.let {
            chatType = it.getString(INTENT_EXTRA_CHAT_TYPE)!!
            fromClientActivation = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            receiverPhotoUrl = it.getString(INTENT_EXTRA_OTHER_USER_IMAGE) ?: ""
            receiverName = it.getString(INTENT_EXTRA_OTHER_USER_NAME) ?: ""
            chatHeaderOrGroupId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: ""
            receiverUserId = it.getString(INTENT_EXTRA_OTHER_USER_ID)!!
            receiverMobileNumber = it.getString(StringConstants.MOBILE_NUMBER.value) ?: ""

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(INTENT_EXTRA_OTHER_USER_IMAGE, receiverPhotoUrl)
            putString(INTENT_EXTRA_OTHER_USER_NAME, receiverName)
            putString(INTENT_EXTRA_CHAT_HEADER_ID, chatHeaderOrGroupId)
            putString(INTENT_EXTRA_CHAT_TYPE, chatType)
            putString(INTENT_EXTRA_OTHER_USER_ID, receiverUserId)
            putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, fromClientActivation)
            putString(StringConstants.MOBILE_NUMBER.value, receiverMobileNumber)
        }
    }

    private fun init() {
        initIntent()
        initListeners()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, BackPressHandler())
    }


    private inner class BackPressHandler() : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            hideSoftKeyboard()
            navigation.navigateBackToChatListIfExistElseOneStepBack()
        }
    }

    private fun subscribeOneToOneViewModel() {

        viewModel.otherUserInfo
                .observe(viewLifecycleOwner, Observer {

                    if (it.name.isNullOrBlank()) {
                        toolbarTitle.text = it.mobile
                    } else {
                        toolbarTitle.text = it.name
                    }

                    if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                        if (Patterns.WEB_URL.matcher(it.imageThumbnailPathInStorage!!).matches()) {

                            Glide.with(requireContext())
                                    .load(Uri.parse(it.imageThumbnailPathInStorage!!))
                                    .placeholder(R.drawable.ic_user)
                                    .error(R.drawable.ic_user)
                                    .into(toolbarUserImageIV)
                        } else {

                            val profilePathRef = if (it.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                                it.imageThumbnailPathInStorage!!
                            else
                                "profile_pics/${it.imageThumbnailPathInStorage}"

                            toolbarUserImageIV.loadImageFromFirebase(profilePathRef,R.drawable.ic_user,R.drawable.ic_user)
                        }
                    } else if (!it.imagePathInStorage.isNullOrBlank()) {

                        if (Patterns.WEB_URL.matcher(it.imagePathInStorage!!).matches()) {

                            Glide.with(requireContext())
                                    .load(Uri.parse(it.imagePathInStorage!!))
                                    .placeholder(R.drawable.ic_user)
                                    .error(R.drawable.ic_user)
                                    .into(toolbarUserImageIV)
                        } else {

                            val profilePathRef = if (it.imagePathInStorage!!.startsWith("profile_pics/"))
                                it.imagePathInStorage!!
                            else
                                "profile_pics/${it.imagePathInStorage}"
                            toolbarUserImageIV.loadImageFromFirebase(profilePathRef,R.drawable.ic_user,R.drawable.ic_user)
                        }

                    } else {
                        toolbarUserImageIV.loadImage(R.drawable.ic_user)
                    }
                })

        viewModel.messages
                .observe(viewLifecycleOwner, Observer {

                    chatRecyclerView.collection = it
                    chatRecyclerView.smoothScrollToLastPosition()
                })

        viewModel.headerInfo
                .observe(viewLifecycleOwner, Observer {

                    if (it.isBlocked) {
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

                            var timeToDisplayText = ""
                            timeToDisplayText = if (DateUtils.isToday(date.time)) {
                                "Last Seen Today At: ${date.toDisplayText()}"
                            } else {
                                "Last Seen ${SimpleDateFormat("MMM dd yyyy").format(date)}"
                            }

                            lastSeenTV.text = timeToDisplayText
                        } else {
                            lastSeenTV.visible()
                            lastSeenTV.text = "Offline"
                        }
                    }
                })
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

        toolBackBtn.setOnClickListener {
            activity?.onBackPressed()
        }

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
                val message = chatFooter.et_message.text.toString().capitalize().trim()
                chatFooter.et_message.setText("")


                if (chatType == ChatConstants.CHAT_TYPE_USER)
                    viewModel.sendNewText(message)
                else
                    groupChatViewModel.sendNewText(message)
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
            toolbarTitle.text =
                    checkForContact(receiverMobileNumber, receiverName!!) //todo check it
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
            PermissionUtils.reqCodePerm -> toolbarTitle.text =
                    checkForContact(receiverMobileNumber, receiverName!!)
            REQUEST_PICK_DOCUMENT -> if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data?.data ?: return
                val uriString = uri.toString()
                val myFile = File(uriString)

                val displayName: String? = getDisplayName(uriString, uri, myFile)

                if (chatType == ChatConstants.CHAT_TYPE_USER)
                    viewModel.sendNewDocumentMessage(
                            requireContext(),
                            "",
                            displayName,
                            uri
                    )
                else
                    groupChatViewModel.sendNewDocumentMessage(
                            text = "",
                            fileName = displayName ?: "Document",
                            uri = uri
                    )


                Log.d(TAG, displayName + "")
                Log.d(TAG, uriString)
            }
            CameraAndGalleryIntegrator.REQUEST_CAPTURE_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_PICK_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_CROP -> {

                if (resultCode == Activity.RESULT_OK) {

                    cameraAndGalleryIntegrator.parseResults(
                            requestCode,
                            resultCode,
                            data,
                            imageCropOptions,
                            this@ChatPageFragment
                    )
                }
            }
            REQUEST_PICK_VIDEO -> {
                val uri = data?.data ?: return

                val uriString = uri.toString()
                val myFile = File(uri.path)

                val videoInfo = getVideoInfo(uriString, uri, myFile)

                if (chatType == ChatConstants.CHAT_TYPE_USER)
                    viewModel.sendNewVideoMessage(
                            requireContext(),
                            "",
                            videoInfo,
                            uri
                    )
                else
                    groupChatViewModel.sendNewVideoMessage(
                            context = requireContext(),
                            text = "",
                            videoInfo = videoInfo,
                            uri = uri
                    )
            }
            REQUEST_GET_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    sendLocationMessage(data)
                }
            }
        }
    }

    private fun sendLocationMessage(data: Intent?) {
        val latitude =
                data!!.getDoubleExtra(CaptureLocationActivity.INTENT_EXTRA_LATITUDE, 0.0)
        val longitude =
                data.getDoubleExtra(CaptureLocationActivity.INTENT_EXTRA_LONGITUDE, 0.0)
        val address =
                data.getStringExtra(CaptureLocationActivity.INTENT_EXTRA_PHYSICAL_ADDRESS)
                        ?: ""
        val imageFile: File? =
                data.getSerializableExtra(CaptureLocationActivity.INTENT_EXTRA_MAP_IMAGE_FILE) as File?

        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendLocationMessage(
                    latitude,
                    longitude,
                    address,
                    imageFile
            )
        else {
            groupChatViewModel.sendLocationMessage(
                    latitude,
                    longitude,
                    address,
                    imageFile
            )
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

    fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()?.getWindowToken(), 0)
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

        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendNewImageMessage(
                    text = "",
                    uri = uri
            )
        else {
            groupChatViewModel.sendNewImageMessage(
                    text = "",
                    uri = uri
            )
        }
    }

    companion object {
        const val TAG = "ChatFragment"

        const val INTENT_EXTRA_CHAT_TYPE = "chat_type"
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
        private const val PDF = "application/pdf"
        private const val XLS = "application/vnd.ms-excel"
    }
}
