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
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatLocalDirectoryReferenceManager
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.VideoInfo
import com.gigforce.core.PermissionUtils
import com.gigforce.core.StringConstants
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.gigforce.modules.feature_chat.swipe.MessageSwipeController
import com.gigforce.modules.feature_chat.swipe.SwipeControllerActions
import com.gigforce.modules.feature_chat.ui.ChatFooter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat.*
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatPageFragment : Fragment(),
    PopupMenu.OnMenuItemClickListener,
    ImageCropCallback,
    SwipeControllerActions {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    //Views
    private lateinit var chatRecyclerView: CoreRecyclerView
    private lateinit var chatFooter: ChatFooter
    private var cameFromLinkInOtherChat: Boolean = false

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

    private val messageSwipeController: MessageSwipeController by lazy {
        MessageSwipeController(requireContext(), this)
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        validateIfRequiredDataIsAvailable()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )

        checkForPermissionElseRequest()
        cancelAnyNotificationIfShown()
        findViews(view)
        init()
        manageNewMessageToContact()
        checkForChatTypeAndSubscribeToRespectiveViewModel()
        hideSoftKeyboard()
        checkIfUserHasSharedAnyFile(arguments)
    }

    private fun checkIfUserHasSharedAnyFile(arguments: Bundle?) {
        val sharedFileBundle = arguments?.getBundle(INTENT_EXTRA_SHARED_FILES_BUNDLE) ?: return

        val imagesShared: ArrayList<Uri>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_IMAGES)
        if (imagesShared != null && imagesShared.isNotEmpty()) {
            cameraAndGalleryIntegrator.startImageCropper(imagesShared.first(), imageCropOptions)
        }

        val videosShared: ArrayList<Uri>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_VIDEOS)
        if (videosShared != null && videosShared.isNotEmpty()) {

            videosShared.forEach {
                sendVideoMessage(it)
            }
        }

        val documentsShared: ArrayList<Uri>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_DOCUMENTS)
        if (documentsShared != null && documentsShared.isNotEmpty()) {

            documentsShared.forEach {
                sendDocumentMessage(it)
            }
        }


    }

    private fun checkForPermissionElseRequest() {
        if (!isStoragePermissionGranted()) {
            askForStoragePermission()
        }
    }

    private fun cancelAnyNotificationIfShown() {

        val mNotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            mNotificationManager.cancel(67)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {

            if (chatType == ChatConstants.CHAT_TYPE_USER) {
                mNotificationManager.cancel(receiverUserId, 0)
            } else {
                mNotificationManager.cancel(chatHeaderOrGroupId, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                otherUserProfilePicture = receiverPhotoUrl,
                otherUserMobileNo = receiverMobileNumber
            )
            adjustUiAccToOneToOneChat()
            subscribeOneToOneViewModel()
        } else if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
            if (chatHeaderOrGroupId.isNullOrBlank()) {
                CrashlyticsLogger.e(
                    TAG,
                    "getting args from arguments",
                    Exception("$chatHeaderOrGroupId <-- String passed as groupId")
                )
                throw IllegalArgumentException("$chatHeaderOrGroupId <-- String passed as groupId")
            }

            groupChatViewModel.setGroupId(chatHeaderOrGroupId!!)
            adjustUiAccToGroupChat()
            subscribeChatGroupViewModel()
        }
    }

    private fun adjustUiAccToOneToOneChat() {

        toolbar.showSubtitle("Offline")
//        tv_lastSeenValue.visible()
//        tv_lastSeenValue.text =
    }

    private fun adjustUiAccToGroupChat() {
        toolbar.hideActionMenu()
        chatFooter.setGroupViewModel(groupChatViewModel)
        chatFooter.enableUserSuggestions()

        toolbar.showSubtitle("Tap to open details")
        toolbar.setSubtitleClickListener(View.OnClickListener {

            val groupId = chatHeaderOrGroupId ?: return@OnClickListener

            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                chatNavigation.openGroupDetailsPage(groupId)
            }
        })

        toolbar.setTitleClickListener(View.OnClickListener {

            val groupId = chatHeaderOrGroupId ?: return@OnClickListener

            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                chatNavigation.openGroupDetailsPage(
                    groupId
                )
            }
        })
    }

    private fun subscribeChatGroupViewModel() {
        groupChatViewModel.outputs
            .groupInfo
            .observe(viewLifecycleOwner, {

                showGroupDetails(it)
                if (it.groupDeactivated) {

                    chatFooter.blockUserInputAndShowMessage("This group is deactivated by an admin")
                    messageSwipeController.disableSwipe()
                } else if (it.currenUserRemovedFromGroup) {

                    chatFooter.blockUserInputAndShowMessage("You have been removed from this group")
                    messageSwipeController.disableSwipe()
                } else if (it.onlyAdminCanPostInGroup) {

                    if (groupChatViewModel.isUserGroupAdmin()) {
                        chatFooter.enableUserInput()
                        messageSwipeController.enableSwipe()
                    } else {

                        chatFooter.blockUserInputAndShowMessage("Only admin can post in this group")
                        messageSwipeController.disableSwipe()
                    }
                } else {

                    chatFooter.enableUserInput()
                    messageSwipeController.enableSwipe()
                }
            })


        groupChatViewModel
            .outputs
            .messages
            .observe(viewLifecycleOwner, { messages ->

                chatRecyclerView.collection = messages.map {
                    ChatMessageWrapper(
                        message = it,
                        oneToOneChatViewModel = viewModel,
                        groupChatViewModel = groupChatViewModel
                    )
                }
                chatRecyclerView.smoothScrollToLastPosition()
            })

        groupChatViewModel
            .inputs
            .getGroupInfoAndStartListeningToMessages()
    }

    private fun showGroupDetails(group: ChatGroup) {
        toolbar.showTitle(group.name)

        if (group.groupAvatarThumbnail.isNotBlank()) {
            toolbar.showImageBehindBackButton(
                group.groupAvatarThumbnail,
                R.drawable.ic_group_white
            )
        } else if (group.groupAvatar.isNotBlank()) {
            toolbar.showImageBehindBackButton(
                group.groupAvatarThumbnail,
                R.drawable.ic_group_white
            )
        } else {
            toolbar.showImageBehindBackButton(R.drawable.ic_group_white)
        }
    }

    private fun findViews(view: View) {
        chatFooter = view.findViewById(R.id.chat_footer)

        chatRecyclerView = view.findViewById(R.id.rv_chat_messages)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(chatRecyclerView)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            cameFromLinkInOtherChat =
                it.getBoolean(INTENT_EXTRA_CAME_FROM_LINK_IN_OTHER_CHAT, false)
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
            cameFromLinkInOtherChat =
                it.getBoolean(INTENT_EXTRA_CAME_FROM_LINK_IN_OTHER_CHAT, false)
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
            putBoolean(INTENT_EXTRA_CAME_FROM_LINK_IN_OTHER_CHAT, cameFromLinkInOtherChat)
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
        initListeners()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            BackPressHandler()
        )
    }


    private inner class BackPressHandler : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            hideSoftKeyboard()

            if (cameFromLinkInOtherChat)
                chatNavigation.navigateUp()
            else
                chatNavigation.navigateBackToChatListIfExistElseOneStepBack()
        }
    }

    private fun subscribeOneToOneViewModel() {

        viewModel.otherUserInfo
            .observe(viewLifecycleOwner, Observer {

                if (it.name.isNullOrBlank()) {
                    toolbar.showTitle(it.mobile)
                } else {
                    toolbar.showTitle(it.name ?: "")
                }

                if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imageThumbnailPathInStorage!!).matches()) {

                        toolbar.showImageBehindBackButton(
                            it.imageThumbnailPathInStorage!!,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )
                    } else {

                        val profilePathRef =
                            if (it.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                                it.imageThumbnailPathInStorage!!
                            else
                                "profile_pics/${it.imageThumbnailPathInStorage}"

                        toolbar.showImageBehindBackButton(
                            profilePathRef,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )
                    }
                } else if (!it.imagePathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imagePathInStorage!!).matches()) {
                        toolbar.showImageBehindBackButton(
                            it.imagePathInStorage!!,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )

                    } else {

                        val profilePathRef =
                            if (it.imagePathInStorage!!.startsWith("profile_pics/"))
                                it.imagePathInStorage!!
                            else
                                "profile_pics/${it.imagePathInStorage}"

                        toolbar.showImageBehindBackButton(
                            profilePathRef,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )
                    }

                } else {

                    toolbar.showImageBehindBackButton(
                        R.drawable.ic_user_white
                    )
                }

                if (it.isUserBlocked) {
                    chatFooter.blockUserInputAndShowMessage("You've blocked this contact")
                    messageSwipeController.disableSwipe()
                } else {
                    chatFooter.enableUserInput()
                    messageSwipeController.enableSwipe()
                }
            })

        viewModel.messages
            .observe(viewLifecycleOwner, { messages ->

                chatRecyclerView.collection = messages.map {
                    ChatMessageWrapper(
                        message = it,
                        oneToOneChatViewModel = viewModel,
                        groupChatViewModel = groupChatViewModel
                    )
                }
                chatRecyclerView.smoothScrollToLastPosition()
            })

        viewModel.headerInfo
            .observe(viewLifecycleOwner, {
                if (it.isBlocked) {
                    chatFooter.blockUserInputAndShowMessage("You've blocked this contact")
                    messageSwipeController.disableSwipe()
                } else {
                    chatFooter.enableUserInput()
                    messageSwipeController.enableSwipe()
                }

                if (it.isOtherUserOnline) {
                    toolbar.showSubtitle("Online")
                } else {
                    if (it.lastUserStatusActivityAt != 0L) {

                        val timeStamp = Timestamp(it.lastUserStatusActivityAt)
                        val date = Date(timeStamp.time)

                        var timeToDisplayText = ""
                        timeToDisplayText = if (DateUtils.isToday(date.time)) {
                            "Last seen today at: ${date.toDisplayText()}"
                        } else {
                            "Last seen ${SimpleDateFormat("MMM dd yyyy").format(date)}"
                        }
                        toolbar.showSubtitle(timeToDisplayText)
                    } else {
                        toolbar.showSubtitle("Offline")
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


    private fun initListeners() {

        toolbar.setBackButtonListener {
            activity?.onBackPressed()
        }

        toolbar.setImageClickListener(View.OnClickListener {

            val groupId = chatHeaderOrGroupId ?: return@OnClickListener

            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                chatNavigation.openGroupDetailsPage(groupId)
            }
        })


        toolbar.setOnOpenActionMenuItemClickListener(View.OnClickListener {

            val popUp = PopupMenu(requireContext(), toolbar.getOptionMenuViewForAnchor())
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_toolbar)
            popUp.menu.findItem(R.id.action_block).title =
                if (chatFooter.isVisible)
                    "Block"
                else
                    "UnBlock"
            popUp.show()
        })

//        toolbarOverflowBtn.setOnClickListener {
//            manageMenu(it)
//        }

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
        putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf(
                MimeTypes.DOC,
                MimeTypes.DOCX,
                MimeTypes.XLS,
                MimeTypes.XLSX,
                MimeTypes.PDF
            )
        )
        startActivityForResult(this, REQUEST_PICK_DOCUMENT)
    }


    private fun manageNewMessageToContact() {
        chatFooter.btn_send.setOnClickListener {
            if (validateNewMessageTask()) {
                val message = chatFooter.et_message.text.toString().capitalize().trim()
                val usersMentioned = chatFooter.getMentionedPeopleInText()

                chatFooter.et_message.setText("")

                if (chatType == ChatConstants.CHAT_TYPE_USER)
                    viewModel.sendNewText(
                            message,
                            chatFooter.getReplyToMessage()
                    )
                else
                    groupChatViewModel.sendNewText(
                            message,
                            usersMentioned,
                            chatFooter.getReplyToMessage()
                    )
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

            toolbar.showTitle(
                checkForContact(receiverMobileNumber, receiverName!!)
            )
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
                    "Please grant storage permission",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionUtils.reqCodePerm -> toolbar.showTitle(
                checkForContact(receiverMobileNumber, receiverName!!)
            )

            REQUEST_PICK_DOCUMENT -> if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                val uri = data?.data ?: return
                sendDocumentMessage(uri)
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
                sendVideoMessage(uri)
            }
            REQUEST_GET_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    sendLocationMessage(data)
                }
            }
        }
    }

    private fun sendDocumentMessage(uri: Uri) {
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
                context = requireContext(),
                text = "",
                fileName = displayName ?: "Document",
                uri = uri
            )


        Log.d(TAG, displayName + "")
        Log.d(TAG, uriString)
    }

    private fun sendVideoMessage(uri: Uri) {
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
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
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

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
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
        sendImageMessage(uri)

        cameraAndGalleryIntegrator.openFrontCamera()
    }

    private fun sendImageMessage(uri: Uri) {
        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = "",
                uri = uri
            )
        else {
            groupChatViewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = "",
                uri = uri
            )
        }
    }

    override fun showReplyUI(chatMessage: ChatMessage) {
            chatFooter.openReplyUi(chatMessage)
    }

    companion object {
        const val TAG = "ChatFragment"

        const val INTENT_EXTRA_CHAT_TYPE = "chat_type"
        const val INTENT_EXTRA_CHAT_HEADER_ID = "chat_header_id"
        const val INTENT_EXTRA_OTHER_USER_ID = "sender_id"
        const val INTENT_EXTRA_OTHER_USER_NAME = "sender_name"
        const val INTENT_EXTRA_OTHER_USER_IMAGE = "sender_profile"
        const val INTENT_EXTRA_CAME_FROM_LINK_IN_OTHER_CHAT = "came_from_link"

        const val INTENT_EXTRA_SHARED_FILES_BUNDLE = "shared_file_bundle"
        const val INTENT_EXTRA_SHARED_IMAGES = "shared_images"
        const val INTENT_EXTRA_SHARED_VIDEOS = "shared_videos"
        const val INTENT_EXTRA_SHARED_DOCUMENTS = "shared_documents"

        private const val REQUEST_PICK_DOCUMENT = 202
        private const val REQUEST_PICK_VIDEO = 204
        private const val REQUEST_GET_LOCATION = 207
        private const val REQUEST_STORAGE_PERMISSION = 205
    }


}
