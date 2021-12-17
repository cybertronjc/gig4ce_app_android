package com.gigforce.modules.feature_chat.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.core.PermissionUtils
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.models.SharedFile
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
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

    private val requestPermissionContract = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

        if (selectedOperation == ChatConstants.OPERATION_PICK_IMAGE && isCameraPermissionGranted() && isStoragePermissionGranted()) {
            pickImage()
            selectedOperation = -1
        } else if (selectedOperation == ChatConstants.OPERATION_PICK_VIDEO && isStoragePermissionGranted()) {
            pickVideo()
            selectedOperation = -1
        } else if (selectedOperation == ChatConstants.OPERATION_PICK_DOCUMENT && isStoragePermissionGranted()) {
            pickDocument()
            selectedOperation = -1
        }
    }

    private val pickVideoContract = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        val uri = it ?: return@registerForActivityResult
        sendVideoMessage(uri)
    }

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    //Views
    private lateinit var chatRecyclerView: CoreRecyclerView
    private lateinit var chatFooter: ChatFooter
    private var cameFromLinkInOtherChat: Boolean = false
    private lateinit var mainChatLayout: View
    private lateinit var needStorageAccessLayout: View
    private lateinit var requestStorageAccessButton: View

    private val viewModel: ChatPageViewModel by viewModels()
    private val groupChatViewModel: GroupChatViewModel by viewModels()

    private val cameraAndGalleryIntegrator: CameraAndGalleryIntegrator by lazy {
        CameraAndGalleryIntegrator(this)
    }

    private val chatFileManager: ChatFileManager by lazy {
        ChatFileManager(requireContext())
    }

    private fun getImageCropOptions(
        shouldCreatedDestinationFile: Boolean
    ): ImageCropOptions {


        return ImageCropOptions
            .Builder()
            .shouldOpenImageCrop(true)
            .setShouldEnableFaceDetector(false)
            .shouldEnableFreeCrop(true).apply {

                if (shouldCreatedDestinationFile) {

                    val image = chatFileManager.createImageFile()
                    setOutputFileUri(image)
                    Log.d("ChatPage", "creating file ...")
                }
            }
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

    //Shared File
    private var sharedFile: SharedFile? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        validateIfRequiredDataIsAvailable()
        findView(view)
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )

        showChatLayout()
        initChat()
    }

    private fun findView(view: View) {
        mainChatLayout = view.findViewById(R.id.main_chat_layout)
        needStorageAccessLayout = view.findViewById(R.id.storage_access_required_layout)
        requestStorageAccessButton = view.findViewById(R.id.storage_access_btn)
    }

    private fun showPermissionRequiredLayout() {
        mainChatLayout.gone()
        needStorageAccessLayout.visible()
    }

    private fun initChat() {
        cancelAnyNotificationIfShown()
        findViews(requireView())
        init()
        manageNewMessageToContact()
        checkForChatTypeAndSubscribeToRespectiveViewModel()
        hideSoftKeyboard()
        checkIfUserHasSharedAnyFile(arguments)
    }

    private fun checkIfUserHasSharedAnyFile(arguments: Bundle?) {
        val sharedFileBundle = arguments?.getBundle(INTENT_EXTRA_SHARED_FILES_BUNDLE) ?: return

        val imagesShared: ArrayList<SharedFile>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_IMAGES)
        if (imagesShared != null && imagesShared.isNotEmpty()) {
            sharedFile = imagesShared.first()
            cameraAndGalleryIntegrator.startImageCropper(
                imagesShared.first().file,
                getImageCropOptions(true)
            )
        }

        val videosShared: ArrayList<SharedFile>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_VIDEOS)
        if (videosShared != null && videosShared.isNotEmpty()) {

            videosShared.forEach {
                sendVideoMessage(
                    it.file,
                    it.text
                )
            }
        }

        val documentsShared: ArrayList<SharedFile>? =
            sharedFileBundle.getParcelableArrayList(INTENT_EXTRA_SHARED_DOCUMENTS)
        if (documentsShared != null && documentsShared.isNotEmpty()) {

            documentsShared.forEach {
                sendDocumentMessage(
                    it.file,
                    it.text
                )
            }
        }


    }

    private fun checkForPermissionElseRequest() {
        if (!isStoragePermissionGranted()) {
            askForStoragePermission()
        }
    }

    private fun handleStorageTreeSelectedResult() {

        if (selectedOperation == ChatConstants.OPERATION_PICK_IMAGE) {

            if (isCameraPermissionGranted()) {
                askForStorageAndCameraPermission()
            } else {
                pickImage()
                selectedOperation = -1
            }
        } else if (selectedOperation == ChatConstants.OPERATION_PICK_VIDEO) {
            pickVideo()
            selectedOperation = -1
        } else if (selectedOperation == ChatConstants.OPERATION_PICK_DOCUMENT) {
            pickDocument()
            selectedOperation = -1
        }
    }

    private fun handleStorageTreeSelectionFailure(
        e: Exception
    ) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select storage")
            .setMessage(e.message.toString())
            .setPositiveButton("Okay") { _, _ -> }
            .show()
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

        toolbar.showSubtitle(getString(R.string.offline_chat))
    }

    private fun adjustUiAccToGroupChat() {
        toolbar.hideActionMenu()
        chatFooter.setGroupViewModel(groupChatViewModel)
        chatFooter.enableUserSuggestions()

        toolbar.showSubtitle(getString(R.string.tap_to_open_details_chat))
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
                    chatFooter.disableInput("This group is deactivated by an admin")
                    messageSwipeController.disableSwipe()
                    //                        userBlockedOrRemovedLayout.text = getString(R.string.group_deactivated_by_admin_chat)
                } else if (it.currenUserRemovedFromGroup) {
                    chatFooter.disableInput("You have been removed from this group")
                    messageSwipeController.disableSwipe()
                    //                        chatFooter.replyBlockedLayout.text = getString(R.string.removed_from_group_chat)
                } else if (it.onlyAdminCanPostInGroup) {
                    chatFooter.visible()

                    if (groupChatViewModel.isUserGroupAdmin()) {
                        chatFooter.enableInput()
                        messageSwipeController.enableSwipe()
                    } else {
                        chatFooter.disableInput("Only admins can post in this group")
                        messageSwipeController.disableSwipe()
//                                                    chatFooter.replyBlockedLayout.text = getString(R.string.only_admin_can_post_chat)
                    }
                } else {
                    chatFooter.enableInput()
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
            .scrollToMessage
            .observe(viewLifecycleOwner, {
                it ?: return@observe
                chatRecyclerView.smoothAndSafeScrollToPosition(it)
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
                group.groupAvatar,
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
                    chatFooter.disableInput("You've blocked this contact")
                    messageSwipeController.disableSwipe()
//                        userBlockedOrRemovedLayout.text = getString(R.string.you_have_blocked_chat)

                } else {
                    chatFooter.enableInput()
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
                    chatFooter.disableInput("You've blocked this contact")
                    messageSwipeController.disableSwipe()
//                        userBlockedOrRemovedLayout.text = getString(R.string.you_have_blocked_chat)
                } else {
                    chatFooter.enableInput()
                    messageSwipeController.enableSwipe()
                }

                if (it.isOtherUserOnline) {
                    toolbar.showSubtitle("Online")
//                        toolbar.showSubtitle(getString(R.string.offline_chat))
                } else {
                    if (it.lastUserStatusActivityAt != 0L) {

                        val timeStamp = Timestamp(it.lastUserStatusActivityAt)
                        val date = Date(timeStamp.time)

                        var timeToDisplayText = ""
                        timeToDisplayText = if (DateUtils.isToday(date.time)) {
                            getString(R.string.last_seen_today_chat) + "${date.toDisplayText()}"
                        } else {
                            getString(R.string.last_seen_chat) + "${
                                SimpleDateFormat("MMM dd yyyy").format(
                                    date
                                )
                            }"
                        }
                        toolbar.showSubtitle(timeToDisplayText)
                    } else {
                        toolbar.showSubtitle(getString(R.string.offline_chat))
                    }
                }
            })

        viewModel
            .scrollToMessage
            .observe(viewLifecycleOwner, {
                it ?: return@observe
                chatRecyclerView.smoothAndSafeScrollToPosition(it)
            })
    }

    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.message_chat))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
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
            val ctw = ContextThemeWrapper(context, R.style.PopupMenuChat)
            val popUp = PopupMenu(ctw, toolbar.getOptionMenuViewForAnchor(), Gravity.END)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_toolbar)
            popUp.menu.findItem(R.id.action_block).title =
                if (chatFooter.isTypingEnabled())
                    getString(R.string.block_chat)
                else
                    getString(R.string.unblock_chat)
            popUp.show()
        })

        chatFooter.attachmentOptionButton.setOnClickListener {
            val popUpMenu = PopupMenu( requireContext(), it)
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
            if (chatFooter.isTypingEnabled())
                BlockUserBottomSheetFragment.launch(
                    viewModel.headerId,
                    viewModel.otherUserId,
                    childFragmentManager
                )
            else
                viewModel.blockOrUnBlockUser(
                    viewModel.headerId,
                    viewModel.otherUserId,
                    false
                )
            true
        }
        R.id.action_report -> {
            ReportUserBottomSheetFragment.launch(
                viewModel.headerId,
                viewModel.otherUserId,
                childFragmentManager
            )
            true
        }
        R.id.action_document -> {
            checkPermissionAndHandleActionPickDocument()
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
            checkPermissionAndHandleActionPickImage()
            true
        }
        R.id.action_video -> {
            checkPermissionAndHandleActionPickVideo()
            true
        }
        else -> {
            false
        }
    }

    private fun checkPermissionAndHandleActionPickImage() {

        if (isCameraPermissionGranted() && isStoragePermissionGranted()) {
            pickImage()
        } else {
            selectedOperation = ChatConstants.OPERATION_PICK_IMAGE

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
                // In case of SDK >= scoped storage
                // we 1. launch document tree contract then
                // 2. camera permission

                if (isStoragePermissionGranted()) {
                    //do something
                } else if (!isCameraPermissionGranted()) {
                    requestPermissions(
                        Manifest.permission.CAMERA
                    )
                }
            } else {

                requestPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun checkPermissionAndHandleActionPickVideo() {
        selectedOperation = ChatConstants.OPERATION_PICK_VIDEO
        pickVideo()
    }

    private fun checkPermissionAndHandleActionPickDocument() {

        if (isStoragePermissionGranted()) {
            pickDocument()
        } else {
            selectedOperation = ChatConstants.OPERATION_PICK_DOCUMENT

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
//                requestDocumentStorageTree()
            } else {
                requestPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun requestPermissions(
        vararg permissions: String
    ) {
        requestPermissionContract.launch(permissions)
    }

    private fun askForStoragePermission() {
        Log.v(TAG, "Permission Required. Requesting Permission")

        if (isStoragePermissionGranted()) {

        }

        requestPermissionContract.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )


        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {

            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun askForStorageAndCameraPermission() {

    }

    private fun pickVideo() {

        try {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = MimeTypes.VIDEO
                startActivityForResult(this, REQUEST_PICK_VIDEO)
            }
        } catch (e: ActivityNotFoundException) {
            showErrorDialog(getString(R.string.no_app_found_to_pick_video_chat))
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
                val message = chatFooter.et_message.text.toString().trim()
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

                chatFooter.closeReplyUi()
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
                showChatLayout()
                initChat()


            } else
                Toast.makeText(
                    requireContext(),
                    getString(R.string.grant_storage_permission_chat),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun showChatLayout() {
        needStorageAccessLayout.gone()
        mainChatLayout.visible()
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
            CameraAndGalleryIntegrator.REQUEST_CROP,
            ImageCropActivity.CROP_RESULT_CODE -> {


                if (resultCode == Activity.RESULT_OK) {

                    cameraAndGalleryIntegrator.parseResults(
                        requestCode,
                        resultCode,
                        data,
                        getImageCropOptions(requestCode == CameraAndGalleryIntegrator.REQUEST_CAPTURE_IMAGE || requestCode == CameraAndGalleryIntegrator.REQUEST_PICK_IMAGE),
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

    private fun sendDocumentMessage(
        uri: Uri,
        text: String? = null
    ) {
        val displayName: String = ImageMetaDataHelpers.getImageName(
            requireContext(),
            uri
        )

        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendNewDocumentMessage(
                requireContext(),
                text ?: "",
                displayName,
                uri
            )
        else
            groupChatViewModel.sendNewDocumentMessage(
                context = requireContext(),
                text = text ?: "",
                fileName = displayName ?: "Document",
                uri = uri
            )
    }

    private fun sendVideoMessage(
        uri: Uri,
        text: String? = null
    ) {
        val videoInfo = ImageMetaDataHelpers.getVideoInfo(
            requireContext(),
            uri
        )

        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendNewVideoMessage(
                requireContext(),
                text ?: "",
                videoInfo,
                uri
            )
        else
            groupChatViewModel.sendNewVideoMessage(
                context = requireContext(),
                text = text ?: "",
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


    private fun isCameraPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isStoragePermissionGranted(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        } else {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
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
        showErrorDialog(e.message ?: getString(R.string.unable_to_capture_and_click_chat))
        FirebaseCrashlytics.getInstance().apply {
            log("Unable to click or capture image")
            recordException(e)
        }
        sharedFile = null
    }

    override fun imageResult(uri: Uri) {
        sendImageMessage(
            uri,
            sharedFile?.text ?: ""
        )
        sharedFile = null
    }

    private fun sendImageMessage(
        uri: Uri,
        text: String
    ) {
        if (chatType == ChatConstants.CHAT_TYPE_USER)
            viewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = text,
                uri = uri
            )
        else {
            groupChatViewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = text,
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
