package com.gigforce.modules.feature_chat.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
//import com.aghajari.emojiview.AXEmojiManager
//import com.aghajari.emojiview.iosprovider.AXIOSEmojiProvider
//import com.aghajari.emojiview.view.AXEmojiView
//import com.aghajari.emojiview.view.AXSingleEmojiView
import com.facebook.shimmer.ShimmerFrameLayout
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatGroup
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.components.cells.AppBar
import com.gigforce.common_ui.listeners.AppBarClicks
import com.gigforce.core.*
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.common_ui.ext.showToast
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
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.gigforce.modules.feature_chat.ui.AttachmentOption
import com.gigforce.modules.feature_chat.ui.AttachmentOptionsListener
import com.gigforce.modules.feature_chat.ui.CommunityFooter
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.IOException


@AndroidEntryPoint
class ChatPageFragment : Fragment(),
    PopupMenu.OnMenuItemClickListener,
    ImageCropCallback,
    SwipeControllerActions, AttachmentOptionsListener, CommunityFooter.RecordingListener {

    @Inject
    lateinit var navigation: INavigation
    
    @Inject
    lateinit var eventTracker: IEventTracker

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
        } else if (selectedOperation == ChatConstants.OPERATION_OPEN_CAMERA && isCameraPermissionGranted() && isStoragePermissionGranted()) {
            openCamera()
            selectedOperation = -1
        } else if (selectedOperation == ChatConstants.OPERATION_PICK_VIDEO && isStoragePermissionGranted()){
            pickAudio()
            selectedOperation = -1
        }
    }

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    //Views
    private lateinit var chatRecyclerView: CoreRecyclerView
    private lateinit var shimmerContainer: View
    private lateinit var chatFooter: ChatFooter
    private lateinit var communityFooter: CommunityFooter
//    private lateinit var audioRecordView: AudioRecordView
    private lateinit var frameLayout: FrameLayout
    private lateinit var rootLayout: FrameLayout
    private var cameFromLinkInOtherChat: Boolean = false
    private lateinit var mainChatLayout: View
//    private lateinit var needStorageAccessLayout: View
//    private lateinit var requestStorageAccessButton: View
    private lateinit var appbar: AppBar
    private var selectedChatMessage: ChatMessage? = null

    var mediaRecorder: MediaRecorder? = null
    var recordFile: String? = null
    var isRecording : Boolean = false
    var localAudioPath : String? = null

//    private var mExoPlayer: SimpleExoPlayer? = null
//    var isPlaying: Boolean = false
//

    private var mExoPlayer: SimpleExoPlayer? = null
    private var isPlaying: Boolean = false
    private var time: Long = 0
    private var currentlyPlayingId: String? = null

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
        setStatusBarIcons(false)
        showChatLayout()
        initChat()
    }

    private fun findView(view: View) {
        mainChatLayout = view.findViewById(R.id.chatMainLayout)
//        needStorageAccessLayout = view.findViewById(R.id.storage_access_required_layout)
//        requestStorageAccessButton = view.findViewById(R.id.storage_access_btn)
        appbar = view.findViewById(R.id.appBarComp)
        appbar.apply {
            changeBackButtonDrawable()
            makeBackgroundMoreRound()
            makeTitleBold()
        }
    }

    private fun showPermissionRequiredLayout() {
        mainChatLayout.gone()
        //needStorageAccessLayout.visible()
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
            var map = mapOf("chat_type" to "Direct")
            eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_SESSION_STARTED, map))
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
            var map = mapOf("chat_type" to "Group")
            eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_SESSION_STARTED, map))
        }
    }

    private fun adjustUiAccToOneToOneChat() {

        //toolbar.showSubtitle(getString(R.string.offline_chat))
        appbar.showSubtitle(getString(R.string.offline_chat))


    }

    private fun adjustUiAccToGroupChat() {
        //toolbar.hideActionMenu()
        appbar.makeMenuItemVisible(false)
//        chatFooter.setGroupViewModel(groupChatViewModel)
//        chatFooter.enableUserSuggestions()

        communityFooter.setGroupViewModel(groupChatViewModel)
        communityFooter.enableUserSuggestions()

        //toolbar.showSubtitle(getString(R.string.tap_to_open_details_chat))
        appbar.showSubtitle(getString(R.string.tap_to_open_details_chat))




    }

    private fun subscribeChatGroupViewModel() {
        groupChatViewModel.outputs
            .groupInfo
            .observe(viewLifecycleOwner, {

                showGroupDetails(it)
                if (it.groupDeactivated) {
                    communityFooter.disableInput("This group is deactivated by an admin")
                    messageSwipeController.disableSwipe()
                    //                        userBlockedOrRemovedLayout.text = getString(R.string.group_deactivated_by_admin_chat)
                } else if (it.currenUserRemovedFromGroup) {
                    communityFooter.disableInput("You have been removed from this group")
                    messageSwipeController.disableSwipe()
                    //                        chatFooter.replyBlockedLayout.text = getString(R.string.removed_from_group_chat)
                } else if (it.onlyAdminCanPostInGroup) {
                    communityFooter.visible()

                    if (groupChatViewModel.isUserGroupAdmin()) {
                        communityFooter.enableInput()
                        messageSwipeController.enableSwipe()
                    } else {
                        communityFooter.disableInput("Only admins can post in this group")
                        messageSwipeController.disableSwipe()
//                                                    chatFooter.replyBlockedLayout.text = getString(R.string.only_admin_can_post_chat)
                    }
                } else {
                    communityFooter.enableInput()
                    messageSwipeController.enableSwipe()
                }
            })


        groupChatViewModel
            .outputs
            .messages
            .observe(viewLifecycleOwner, { messages ->
                messages.let {
                    chatRecyclerView.collection = messages.map {
                        ChatMessageWrapper(
                            message = it,
                            oneToOneChatViewModel = viewModel,
                            groupChatViewModel = groupChatViewModel,
                            viewLifecycleOwner
                        )
                    }
                    chatRecyclerView.smoothScrollToLastPosition()
                    chatRecyclerView.visible()
                    shimmerContainer.gone()
                }


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

        groupChatViewModel.selectedChatMessage.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            Log.d("selectedMsg", "${it.flowType} , ${it.type} , ${it.chatType}")
            selectedChatMessage = it
            var isCopyEnable = false
            var isDeleteEnable = false
            var isInfoEnable = false
            var isDownloadEnable = false
            isDeleteEnable = it.flowType == "out"
            isInfoEnable = it.flowType == "out" && it.chatType == "group"
            if(it.type == "text"){
                isCopyEnable = true
                isDeleteEnable = it.flowType == "out"
            }
            appbar.makeChatOptionsVisible(true, isCopyEnable, isDeleteEnable, isInfoEnable, isDownloadEnable)

        })

    }

    private fun showGroupDetails(group: ChatGroup) {
        appbar.setAppBarTitle(group.name)

        if (group.groupAvatarThumbnail.isNotBlank()) {

            appbar.showMainImageView(
                group.groupAvatarThumbnail,
                R.drawable.ic_group_white
            )
        } else if (group.groupAvatar.isNotBlank()) {
            appbar.showMainImageView(
                group.groupAvatar,
                R.drawable.ic_group_white
            )
        } else {
            appbar.showMainImageView(R.drawable.ic_group_white)
        }
    }

    private fun findViews(view: View) {
        frameLayout = view.findViewById(R.id.layoutMain)
        chatFooter = view.findViewById(R.id.chat_footer)
        rootLayout = view.findViewById(R.id.layoutRoot)
        communityFooter = view.findViewById(R.id.community_footer)
        shimmerContainer = view.findViewById(R.id.shimmer_controller)
        //communityFooter = context?.let { CommunityFooter(it) }!!
        chatRecyclerView = view.findViewById(R.id.rv_chat_messages)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(chatRecyclerView)
        setUpAudioView()
    }

    private fun setUpAudioView() {
        communityFooter.setAttachmentOptions(AttachmentOption.defaultList, this)
        communityFooter.setRecordingListener(this)
//        AXEmojiManager.install(activity, AXIOSEmojiProvider(activity))
//        val emojiView = AXEmojiView(activity)
//        communityFooter.setupEmojiLayout(emojiView)

        rootLayout.setOnTouchListener { v, event ->
            communityFooter.hideAttachmentOptionView()
            true
        }
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
                    appbar.setAppBarTitle(it.mobile)
                } else {
                    appbar.setAppBarTitle(it.name ?: "")
                }

//                if(it.mobile.isNotBlank()){
//                    receiverMobileNumber = it.mobile
//                    Log.d(TAG, "mobile: $receiverMobileNumber")
//                }

                if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imageThumbnailPathInStorage!!).matches()) {

                        appbar.showMainImageView(
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

                        appbar.showMainImageView(
                            profilePathRef,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )
                    }
                } else if (!it.imagePathInStorage.isNullOrBlank()) {

                    if (Patterns.WEB_URL.matcher(it.imagePathInStorage!!).matches()) {
                        appbar.showMainImageView(
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

                        appbar.showMainImageView(
                            profilePathRef,
                            R.drawable.ic_user_white,
                            R.drawable.ic_user_white
                        )
                    }

                } else {

                    appbar.showMainImageView(
                        R.drawable.ic_user_white
                    )
                }

                if (it.isUserBlocked) {
                    //chatFooter.disableInput("You've blocked this contact")
                        communityFooter.disableInput("You've blocked this contact")
                        messageSwipeController.disableSwipe()
//                        userBlockedOrRemovedLayout.text = getString(R.string.you_have_blocked_chat)

                } else {
                    //chatFooter.enableInput()
                        communityFooter.enableInput()
                        messageSwipeController.enableSwipe()
                }
            })

        viewModel.messages
            .observe(viewLifecycleOwner, { messages ->

                chatRecyclerView.collection = messages.map {
                    ChatMessageWrapper(
                        message = it,
                        oneToOneChatViewModel = viewModel,
                        groupChatViewModel = groupChatViewModel,
                        lifeCycleOwner = viewLifecycleOwner
                    )
                }
                chatRecyclerView.smoothScrollToLastPosition()
                chatRecyclerView.visible()
                shimmerContainer.gone()
            })

        viewModel.headerInfo
            .observe(viewLifecycleOwner, {

                if (it.isBlocked) {
                    //chatFooter.disableInput("You've blocked this contact")
                    communityFooter.disableInput("You've blocked this contact")
                    messageSwipeController.disableSwipe()
//                        userBlockedOrRemovedLayout.text = getString(R.string.you_have_blocked_chat)
                } else {
                    //chatFooter.enableInput()
                    communityFooter.enableInput()
                    messageSwipeController.enableSwipe()
                }

                if (it.isOtherUserOnline) {
                    appbar.showSubtitle("Online")
                    appbar.makeOnlineImageVisible(visible = true)
                } else {
                    appbar.showSubtitle(getString(R.string.offline_chat))
                    appbar.makeOnlineImageVisible(false)
//                    if (it.lastUserStatusActivityAt != 0L) {
//
//                        val timeStamp = Timestamp(it.lastUserStatusActivityAt)
//                        val date = Date(timeStamp.time)
//
//                        var timeToDisplayText = ""
//                        timeToDisplayText = if (DateUtils.isToday(date.time)) {
//                            getString(R.string.last_seen_today_chat) + "${date.toDisplayText()}"
//                        } else {
//                            getString(R.string.last_seen_chat) + "${
//                                SimpleDateFormat("MMM dd yyyy").format(
//                                    date
//                                )
//                            }"
//                        }
//                        appbar.showSubtitle(timeToDisplayText)
//                        appbar.makeOnlineImageVisible(false)
//                    } else {
//                        appbar.showSubtitle(getString(R.string.offline_chat))
//                        appbar.makeOnlineImageVisible(false)
//                    }
                }
            })

        viewModel
            .scrollToMessage
            .observe(viewLifecycleOwner, {
                it ?: return@observe
                chatRecyclerView.smoothAndSafeScrollToPosition(it)
            })

        viewModel.selectedChatMessage.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            Log.d("selectedMsg", "${it.flowType} , ${it.type} , ${it.chatType}")
            selectedChatMessage = it
            var isCopyEnable = false
            var isDeleteEnable = false
            var isInfoEnable = false
            var isDownloadEnable = false
            isDeleteEnable = it.flowType == "out"
            isInfoEnable = it.flowType == "out" && it.chatType == "group"
            if(it.type == "text"){
                isCopyEnable = true
                isDeleteEnable = it.flowType == "out"
            }
            appbar.makeChatOptionsVisible(true, isCopyEnable, isDeleteEnable, isInfoEnable, isDownloadEnable)
        })

        viewModel.audioData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            Log.d("selectedMsg", "${it.playPause} , ${it.isAudioPlaying} , ${it.currentlyPlayingAudioId} , ${it.playingAudioUri}")

            if (it.playPause == true) {
                val mediaSource = extractMediaSourceFromUri(it.playingAudioUri!!)
                //play the audio if previously nothing ws playing
                if (currentlyPlayingId == it.currentlyPlayingAudioId) {
                    //resume the audio
                } else {
                    //play my audio
                    //playPauseBuild(true, it.currentlyPlayingAudioId.toString(), it.playingAudioUri!!)
                    //play(mediaSource)
                    //playMyAudio(it.playingAudioUri!!)
                }
            } else {
                    //pause the audio
                    //pause()
                    //playPauseBuild(false, it.currentlyPlayingAudioId.toString(), it.playingAudioUri!!)
            }

        })

        chatRecyclerView.visible()
        shimmerContainer.gone()

    }
    private fun extractMediaSourceFromUri(uri: Uri): MediaSource {
        val userAgent = context?.let { Util.getUserAgent(it, "Exo") }
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(uri)
    }

    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.message_chat))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
            .show()
    }


    private fun initListeners() {

//        toolbar.setBackButtonListener {
//            activity?.onBackPressed()
//        }

        appbar.setImageClickListener(View.OnClickListener {
            val groupId = chatHeaderOrGroupId ?: return@OnClickListener
            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false

                ))
            }
            else if(chatType == ChatConstants.CHAT_TYPE_USER) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false
                ))
            }
        })

        appbar.setSubtitleClickListener(View.OnClickListener {
            val groupId = chatHeaderOrGroupId ?: return@OnClickListener

            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false

                ))
            }
            else if(chatType == ChatConstants.CHAT_TYPE_USER) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false
                ))
            }
        })
        appbar.setTitleClickListener(View.OnClickListener {
            val groupId = chatHeaderOrGroupId ?: return@OnClickListener

            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false
                ))
            }
            else if(chatType == ChatConstants.CHAT_TYPE_USER) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to false

                ))
            }
        })

        appbar.setBackButtonListener(View.OnClickListener {
            hideSoftKeyboard()

            if (cameFromLinkInOtherChat)
                chatNavigation.navigateUp()
            else
                chatNavigation.navigateBackToChatListIfExistElseOneStepBack()
        })

        appbar.setCopyClickListener(View.OnClickListener {
            selectedChatMessage?.let {
                if(it.type == "text"){
                    copyMessageToClipBoard(it.content)
                }
            }
//            viewModel.makeSelectEnable(false)
//            groupChatViewModel.makeSelectEnable(false)
//            appbar.makeChatOptionsVisible(false, false, false)
            disableChatSelection()
        })

        appbar.setForwardClickListener(View.OnClickListener {
            selectedChatMessage?.let { it1 -> forwardMessage(it1) }
            disableChatSelection()
        })

        appbar.setInfoClickListener(View.OnClickListener {
            selectedChatMessage?.let { it1 ->
                Log.d(TAG, "info: ${it1.groupId} , ${it1.id}")
                viewMessageInfo(it1.groupId, it1.id)
            }
            disableChatSelection()
        })

        appbar.setReplyClickListener(View.OnClickListener {
            selectedChatMessage?.let { it1 -> communityFooter.openReplyUi(it1) }
            disableChatSelection()
        })


        communityFooter.viewBinding.imageViewCamera.setOnClickListener {
            communityFooter.hideAttachmentOptionView()
            checkCameraPermissionAndHandleActionOpenCamera()
        }

        appbar.setChatOptionsCancelListener(View.OnClickListener {
            disableChatSelection()
        })

        appbar.setDeleteClickListener(View.OnClickListener {
            selectedChatMessage.let {
                if(it?.chatType == "user"){
                    viewModel.deleteMessage(
                        it?.id
                    )
                    viewModel.makeSelectEnable(false)
                } else if (it?.chatType == "group"){
                    groupChatViewModel.deleteMessage(
                        it?.id
                    )
                    groupChatViewModel.makeSelectEnable(false)
                }
//                viewModel.makeSelectEnable(false)
//                groupChatViewModel.makeSelectEnable(false)
//                appbar.makeChatOptionsVisible(false, false, false)
                disableChatSelection()
            }
        })

        appbar.setOnOpenActionMenuItemClickListener(View.OnClickListener {
            val ctw = ContextThemeWrapper(context, R.style.PopupMenuChat)
            val popUp = PopupMenu(ctw, appbar.getOptionMenuViewForAnchor(), Gravity.END)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_toolbar)
            popUp.menu.findItem(R.id.action_block).title =
                if (communityFooter.isTypingEnabled())
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

    private fun disableChatSelection(){
        selectedChatMessage = null
        viewModel.makeSelectEnable(false)
        groupChatViewModel.makeSelectEnable(false)
        appbar.makeChatOptionsVisible(false, false, false, false, false)
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_block -> {
            if (communityFooter.isTypingEnabled()) {
                BlockUserBottomSheetFragment.launch(
                    viewModel.headerId,
                    viewModel.otherUserId,
                    childFragmentManager
                )
            } else {
                viewModel.blockOrUnBlockUser(
                    viewModel.headerId,
                    viewModel.otherUserId,
                    false
                )
                eventTracker.pushEvent(
                    TrackingEventArgs(
                        CommunityEvents.EVENT_CHAT_UNBLOCKED_USER,
                        null
                    )
                )
            }
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
        R.id.action_media -> {
            val groupId = chatHeaderOrGroupId ?: ""
            if (chatType == ChatConstants.CHAT_TYPE_GROUP) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_GROUP,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to true

                ))
            }
            else if(chatType == ChatConstants.CHAT_TYPE_USER) {
                navigation.navigateTo("chats/userGroupDetailsFragment", bundleOf(
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_IMAGE to receiverPhotoUrl,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_NAME to receiverName,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_CHAT_HEADER_ID to groupId,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_OTHER_USER_ID to receiverUserId,
                    StringConstants.MOBILE_NUMBER.value to receiverMobileNumber,
                    UserAndGroupDetailsFragment.INTENT_EXTRA_SHOW_MEDIA_ONLY to true
                ))
            }
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
               if (!isCameraPermissionGranted()) {
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

    private fun checkPermissionAndHandleActionPickAudio() {
        if (isStoragePermissionGranted()) {
            pickAudio()
        } else {
            selectedOperation = ChatConstants.OPERATION_PICK_AUDIO

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

    private fun checkPermissionAndHandleActionRecordAudio(){

        if (isAudioRecordPermissionGranted() && isStoragePermissionGranted()){
            //start recording
            startRecording()
        } else{
            selectedOperation = ChatConstants.OPERATION_START_AUDIO
            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
                // In case of SDK >= scoped storage
                // we 1. launch document tree contract then
                // 2. camera permission
                if (!isAudioRecordPermissionGranted()) {
                    requestPermissions(
                        Manifest.permission.RECORD_AUDIO
                    )
                }
            } else {

                requestPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                )
            }

        }
    }

    private fun checkCameraPermissionAndHandleActionOpenCamera(){
        if (isCameraPermissionGranted() && isStoragePermissionGranted()) {
            openCamera()
        } else {
            selectedOperation = ChatConstants.OPERATION_OPEN_CAMERA

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
                // In case of SDK >= scoped storage
                // we 1. launch document tree contract then
                // 2. camera permission
                if (!isCameraPermissionGranted()) {
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

    private fun pickAudio() {
        try {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = MimeTypes.AUDIO
                startActivityForResult(this, REQUEST_PICK_AUDIO)
            }
        } catch (e: ActivityNotFoundException) {
            showErrorDialog(getString(R.string.no_app_found_to_pick_audio_chat))
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

    private fun openCamera(){
        cameraAndGalleryIntegrator.startCameraForCapturing()
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
        communityFooter.viewBinding.imageViewSend.setOnClickListener {
            if(validateNewMessageTask()){
                val message = communityFooter.viewBinding.editTextMessage.text.toString().trim()
                val usersMentioned = communityFooter.getMentionedPeopleInText()

                communityFooter.viewBinding.editTextMessage.setText("")

                var type = ""
                if (chatType == ChatConstants.CHAT_TYPE_USER) {
                    viewModel.sendNewText(
                        message,
                        communityFooter.getReplyToMessage()
                    )
                    type = "Direct"
                } else {
                    groupChatViewModel.sendNewText(
                        message,
                        usersMentioned,
                        communityFooter.getReplyToMessage()
                    )

                communityFooter.closeReplyUi()
                    type = "Group"
                }
                var map = mapOf("chat_type" to type, "message_type" to "Text")
                eventTracker.pushEvent(
                    TrackingEventArgs(
                        CommunityEvents.EVENT_CHAT_MESSAGE_SENT,
                        map
                    )
                )
                chatFooter.closeReplyUi()
            }
        }
    }

    private fun validateNewMessageTask(): Boolean {

        if (communityFooter.viewBinding.editTextMessage.text.toString().isBlank()) {
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
            appbar.setAppBarTitle(checkForContact(receiverMobileNumber, receiverName!!))
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
        //needStorageAccessLayout.gone()
        mainChatLayout.visible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionUtils.reqCodePerm -> appbar.setAppBarTitle(
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
            REQUEST_PICK_AUDIO -> {
                val uri = data?.data ?: return
                sendAudioMessage(uri, "")
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
        var type = ""
        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            viewModel.sendNewDocumentMessage(
                requireContext(),
                text ?: "",
                displayName,
                uri
            )
            type = "Direct"
        } else {
            groupChatViewModel.sendNewDocumentMessage(
                context = requireContext(),
                text = text ?: "",
                fileName = displayName ?: "Document",
                uri = uri
            )
            type = "Group"
        }
        var map = mapOf("chat_type" to type, "message_type" to "Document")
        eventTracker.pushEvent(
            TrackingEventArgs(
                CommunityEvents.EVENT_CHAT_MESSAGE_SENT,
                map
            )
        )

    }

    private fun sendAudioMessage(
        uri: Uri,
        text: String? = null
    ) {

        val audioInfo = ImageMetaDataHelpers.getAudioInfo(
            requireContext(),
            uri
        )

        //Log.d("audiolength", "info: ${audioInfo.duration} , size: ${audioInfo.size} record: $recordTime")

        if (chatType == ChatConstants.CHAT_TYPE_USER){
            viewModel.sendNewAudioMessage(
                requireContext(),
                text ?: "",
                uri,
                audioInfo
            )
        } else {
            groupChatViewModel.sendNewAudioMessage(
                requireContext(),
                text ?: "",
                uri,
                audioInfo
            )
        }

    }

    private fun sendVideoMessage(
        uri: Uri,
        text: String? = null
    ) {
        val videoInfo = ImageMetaDataHelpers.getVideoInfo(
            requireContext(),
            uri
        )
        var type = ""
        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            viewModel.sendNewVideoMessage(
                requireContext(),
                text ?: "",
                videoInfo,
                uri
            )
            type = "Direct"
        } else {
            groupChatViewModel.sendNewVideoMessage(
                context = requireContext(),
                text = text ?: "",
                videoInfo = videoInfo,
                uri = uri
            )
            type = "Group"
        }
        var map = mapOf("chat_type" to type, "message_type" to "Video")
        eventTracker.pushEvent(
            TrackingEventArgs(
                CommunityEvents.EVENT_CHAT_MESSAGE_SENT,
                map
            )
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

        var type = ""
        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            viewModel.sendLocationMessage(
                latitude,
                longitude,
                address,
                imageFile
            )
            type = "Direct"
        } else {
            groupChatViewModel.sendLocationMessage(
                latitude,
                longitude,
                address,
                imageFile
            )
            type = "Group"
        }
        var map = mapOf("chat_type" to type, "message_type" to "Location")
        eventTracker.pushEvent(
            TrackingEventArgs(
                CommunityEvents.EVENT_CHAT_MESSAGE_SENT,
                map
            )
        )
    }


    private fun isCameraPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isAudioRecordPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
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

    private fun copyMessageToClipBoard(text: String) {
        val clip: ClipData = ClipData.newPlainText("Copy", text)
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
            clip
        )
        showToast("Copied")
    }

    private fun forwardMessage(msg: ChatMessage){
        if (msg != null){
            navigation.navigateTo("chats/contactsFragment", bundleOf(
                ChatConstants.INTENT_EXTRA_FORWARD_MESSAGE to msg
            )
            )
        }
    }

    private fun viewMessageInfo(groupId: String, messageId: String) {
        navigation.navigateTo("chats/messageInfo",
            bundleOf(
                GroupMessageViewInfoFragment.INTENT_EXTRA_GROUP_ID to groupId,
                GroupMessageViewInfoFragment.INTENT_EXTRA_MESSAGE_ID to messageId
            )
        )
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
        var type = ""
        if (chatType == ChatConstants.CHAT_TYPE_USER) {
            viewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = text,
                uri = uri
            )
            type = "Direct"
        } else {
            groupChatViewModel.sendNewImageMessage(
                context = requireContext().applicationContext,
                text = text,
                uri = uri
            )
            type = "Group"
        }
        var map = mapOf("chat_type" to type, "message_type" to "Image")
        eventTracker.pushEvent(
            TrackingEventArgs(
                CommunityEvents.EVENT_CHAT_MESSAGE_SENT,
                map
            )
        )
    }

    override fun showReplyUI(chatMessage: ChatMessage) {
        communityFooter.openReplyUi(chatMessage)
    }

    override fun onPause() {
        super.onPause()
        var type = ""
        if(chatType == ChatConstants.CHAT_TYPE_USER) type = "Direct" else type = "Group"
        var map = mapOf("chat_type" to type)
        eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_SESSION_ENDED, map))
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
        private const val REQUEST_PICK_AUDIO = 209
        private const val REQUEST_GET_LOCATION = 207
        private const val REQUEST_STORAGE_PERMISSION = 205
    }

    override fun onClick(attachmentOption: AttachmentOption?) {
        when (attachmentOption?.id) {
            AttachmentOption.DOCUMENT_ID -> {
                checkPermissionAndHandleActionPickDocument()
//                showToast("Document Clicked")
            }
            AttachmentOption.CAMERA_ID -> {
                checkCameraPermissionAndHandleActionOpenCamera()
//                showToast("Camera Clicked")
            }
            AttachmentOption.GALLERY_ID -> {
                checkPermissionAndHandleActionPickImage()
//                showToast("Gallery Clicked")
            }
            AttachmentOption.AUDIO_ID -> {
//                showToast("Audio Clicked")
                checkPermissionAndHandleActionPickAudio()
            }
            AttachmentOption.LOCATION_ID -> {
                startActivityForResult(
                    Intent(requireContext(), CaptureLocationActivity::class.java),
                    REQUEST_GET_LOCATION
                )
//                showToast("Location Clicked")
            }
            AttachmentOption.CONTACT_ID -> {
                showToast("Coming soon")
                //checkPermissionAndHandleActionPickVideo()
            }
        }
    }

    private fun startRecording() {
        //showToast("started record")
        //val recordPath : String = activity?.getExternalFilesDir("/")!!.absolutePath
        isRecording = true
        val formatter : SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.ROOT)
        val now: Date = Date()
        recordFile = "Recording_${formatter.format(now)}.mp3"
        //localAudioPath = "$recordPath/$recordFile"
        //record_status_text.setText("Recording audionote")

        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaRecorder!!.setOutputFile(File(chatFileManager.audioFilesDirectory,recordFile))
        }
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mediaRecorder!!.prepare()
        }catch (e: IOException){
            e.printStackTrace()
        }
        mediaRecorder!!.start()
        Log.d(TAG, "recording has started")
    }

    private fun stopRecording() {
        isRecording = false
        if (mediaRecorder != null){
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
            Log.d(TAG, "recording has stopped")
        }

    }


    override fun onStart() {
        super.onStart()
        // connect the controllers again to the session
        // without this connect() you won't be able to start the service neither control it with the controller
    }

    override fun onResume() {
        super.onResume()
        disableChatSelection()
    }

    override fun onStop() {
        if (isRecording){
            stopRecording()
        }
        setStatusBarIcons(true)
        super.onStop()
    }


    override fun onRecordingStarted() {
        //showToast("Recording started")
        time = System.currentTimeMillis() / (1000);
        checkPermissionAndHandleActionRecordAudio()
    }

    override fun onRecordingLocked() {
        showToast("Recording locked")
    }

    override fun onRecordingCompleted() {
        //showToast("Recording completed")
        val recordTime = (System.currentTimeMillis() / 1000 - time).toInt()
        //Log.d("audio", "length: $recordTime")
        if (isRecording && recordTime > 1){
            val localUri = Uri.fromFile(File(chatFileManager.audioFilesDirectory,recordFile))
            sendAudioMessage(
                localUri,
                ""
            )
            //Log.d("record", "stopped $localUri")
            isRecording = false
            stopRecording()
        }

    }

    override fun onRecordingCanceled() {
        //showToast("Recording canceled")
        isRecording = false
        stopRecording()
    }

    fun setStatusBarIcons(shouldChangeStatusBarTintToDark: Boolean){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor: View = activity?.window?.decorView!!
            if (shouldChangeStatusBarTintToDark) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // We want to change tint color to white again.
                // You can also record the flags in advance so that you can turn UI back completely if
                // you have set other flags before, such as translucent or full screen.
                decor.systemUiVisibility = 0
            }
        }
    }

}
