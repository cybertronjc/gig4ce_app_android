package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.location.LocationSharingActivity
import com.gigforce.common_ui.location.LocationUpdatesService
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class LocationMessageView(
    val type: MessageFlowType,
    val messageType: MessageType,
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs),
    IViewHolder,
    View.OnClickListener,
    View.OnLongClickListener,
    PopupMenu.OnMenuItemClickListener,
    BaseChatMessageItemView{

    @Inject
    lateinit var navigation : INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private lateinit var imageView: ImageView
    private lateinit var locationAddressTV: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: LinearLayout
    private lateinit var frameLayoutRoot: FrameLayout
    private lateinit var receivedStatusIV: ImageView
    private lateinit var senderNameTV: TextView
    private lateinit var stopSharingTV: TextView

    private var selectedMessageList = emptyList<ChatMessage>()

    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private lateinit var message: ChatMessage
    private lateinit var oneToOneChatViewModel: ChatPageViewModel
    private lateinit var groupChatViewModel: GroupChatViewModel


    init {
        setDefault()
        inflate()
        findViews()
        setOnClickListeners()
    }

    private fun findViews() {

        senderNameTV = this.findViewById(R.id.user_name_tv)
        imageView = this.findViewById(R.id.iv_image)
        frameLayoutRoot = this.findViewById(R.id.frame)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        locationAddressTV = this.findViewById(R.id.location_address_tv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
        stopSharingTV = this.findViewById(R.id.stop_location)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val resId = if (type == MessageFlowType.IN)
            R.layout.recycler_item_chat_location_in
        else
            R.layout.recycler_item_chat_location_out

        LayoutInflater.from(context).inflate(resId, this, true)
    }

    private fun setOnClickListeners() {
        cardView.setOnClickListener(this)
        cardView.setOnLongClickListener(this)
        senderNameTV.setOnClickListener(this)
    }

    private fun loadThumbnail(msg: ChatMessage) {
        if (msg.thumbnailBitmap != null) {

            Glide.with(context)
                .load(msg.thumbnailBitmap)
                .placeholder(getCircularProgressDrawable())
                .centerCrop()
                .into(imageView)
        } else if (msg.thumbnail != null) {

            val thumbnailStorageRef = firebaseStorage.reference.child(msg.thumbnail!!)
            Glide.with(context)
                .load(thumbnailStorageRef)
                .placeholder(getCircularProgressDrawable())
                .centerCrop()
                .into(imageView)
        } else if (msg.attachmentPath != null) {
            val thumbnailStorageRef = firebaseStorage.reference.child(msg.attachmentPath!!)
            Glide.with(context)
                .load(thumbnailStorageRef)
                .placeholder(getCircularProgressDrawable())
                .centerCrop()
                .into(imageView)
        }
    }

    override fun bind(data: Any?) {
        data?.let {
            val dataAndViewModels = it as ChatMessageWrapper
            message = dataAndViewModels.message
            groupChatViewModel = dataAndViewModels.groupChatViewModel
            oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

            dataAndViewModels.lifeCycleOwner?.let { it1 ->
                if (messageType == MessageType.ONE_TO_ONE_MESSAGE){
                    oneToOneChatViewModel.enableSelect.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == false) {
                            frameLayoutRoot?.foreground = null
                        }
                    })
                    oneToOneChatViewModel.selectedChatMessage.observe(it1, Observer {
                        it ?: return@Observer
                        selectedMessageList = it
                        if (it.isNotEmpty() && it.contains(message)){
                            Log.d("MultiSelection", "Contains this message $it")
                            frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        } else {
                            frameLayoutRoot.foreground = null
                        }

                    })
                    oneToOneChatViewModel.scrollToMessageId.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == message.id){
                            blinkLayout()
                        }
                    })
                } else if(messageType == MessageType.GROUP_MESSAGE){
                    groupChatViewModel.enableSelect.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == false) {
                            frameLayoutRoot?.foreground = null
                        }
                    })
                    groupChatViewModel.selectedChatMessage.observe(it1, Observer {
                        it ?: return@Observer
                        selectedMessageList = it
                        if (it.isNotEmpty() && it.contains(message)){
                            Log.d("MultiSelection", "Contains this message $it")
                            frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
                        } else {
                            frameLayoutRoot.foreground = null
                        }

                    })
                    groupChatViewModel.scrollToMessageId.observe(it1, Observer {
                        it ?: return@Observer
                        if (it == message.id){
                            blinkLayout()
                        }
                    })
                }

            }

            senderNameTV.isVisible =
                messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = message.senderInfo.name

            if (message.isLiveLocation) {
                if (message.isCurrentlySharingLiveLocation){
                    if (message.senderInfo.id == FirebaseAuth.getInstance().currentUser?.uid) {
                        stopSharingTV.text = "Sharing live location"
                        stopSharingTV.visible()
                        locationAddressTV.gone()
                    } else {
                        stopSharingTV.text = "View live location"
                        stopSharingTV.visible()
                        locationAddressTV.gone()
                    }
                } else {
                    stopSharingTV.text = "Live location ended"
                    stopSharingTV.visible()
                    locationAddressTV.gone()
                }

            } else {
                stopSharingTV.gone()
                locationAddressTV.visible()
                locationAddressTV.text = "\uD83D\uDCCD ${message.locationPhysicalAddress}"
            }

            textViewTime.setText(message.timestamp?.toDisplayText())
            loadThumbnail(message)
            setReceivedStatus(message)

//            if(msg.thumbnailBitmap != null){
//                handleLocationUploading()
//            } else {
//                handleLocationUploaded()
//            }
        }
    }

    private fun handleLocationUploaded() {

    }

    private fun handleLocationUploading() {

    }

//    private fun isCurrentlySharingLocation(): Boolean{
//        return message.updatedAt?.toDate().after()
//    }

    private fun blinkLayout(){
        frameLayoutRoot.foreground = resources.getDrawable(R.drawable.selected_chat_foreground)
        Handler(Looper.getMainLooper()).postDelayed({
            frameLayoutRoot.foreground = null
            if (messageType == MessageType.GROUP_MESSAGE){
                groupChatViewModel.setScrollToMessageNull()
            } else {
                oneToOneChatViewModel.setScrollToMessageNull()
            }
        },2000)
    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    private fun setReceivedStatus(msg: ChatMessage) {
        when (msg.status) {
            ChatConstants.MESSAGE_STATUS_NOT_SENT -> {
                Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
            }
            ChatConstants.MESSAGE_STATUS_DELIVERED_TO_SERVER -> {
                Glide.with(context)
                    .load(R.drawable.ic_msg_sent)
                    .into(receivedStatusIV)
            }
            ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER -> {
                Glide.with(context)
                    .load(R.drawable.ic_msg_delivered)
                    .into(receivedStatusIV)
            }
            ChatConstants.MESSAGE_STATUS_READ_BY_USER -> {
                Glide.with(context)
                    .load(R.drawable.ic_msg_seen)
                    .into(receivedStatusIV)
            }
            else -> {
                Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(receivedStatusIV)
            }
        }
    }


    override fun onClick(v: View?) {
        if (v?.id == R.id.ll_msgContainer) {
            if (!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
                //Launch Map
                if (message == null)
                    return

                if (message.isLiveLocation) {
                    if (message.isCurrentlySharingLiveLocation) {
                        val intent = Intent(context, LocationSharingActivity::class.java)
                        intent.putExtra(AppConstants.INTENT_EXTRA_CHAT_TYPE, message.chatType)
                        intent.putExtra(AppConstants.INTENT_EXTRA_CHAT_HEADER_ID, message.headerId)
                        intent.putExtra(AppConstants.INTENT_EXTRA_CHAT_MESSAGE_ID, message.id)
                        context.startActivity(intent)
                    }
                } else {
                    val lat = message.location?.latitude ?: 0.0
                    val long = message.location?.longitude ?: 0.0

                    if (lat != 0.0) {
                        val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Location)"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_app_found_locations_chat),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            } else {
                if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                    if (selectedMessageList.contains(message)) {
                        //remove
                        frameLayoutRoot.foreground = null
                        oneToOneChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground =
                            resources.getDrawable(R.drawable.selected_chat_foreground)
                        oneToOneChatViewModel.selectChatMessage(message, true)
                    }
                } else if (messageType == MessageType.GROUP_MESSAGE) {
                    if (selectedMessageList.contains(message)) {
                        //remove
                        frameLayoutRoot.foreground = null
                        groupChatViewModel.selectChatMessage(message, false)
                    } else {
                        //add
                        frameLayoutRoot.foreground =
                            resources.getDrawable(R.drawable.selected_chat_foreground)
                        groupChatViewModel.selectChatMessage(message, true)
                    }

                }
            }
        } else if (v?.id == R.id.user_name_tv){
            //navigate to chat page
            navigation.popBackStack()
            chatNavigation.navigateToChatPage(
                chatType = com.gigforce.common_ui.chat.ChatConstants.CHAT_TYPE_USER,
                otherUserId = message.senderInfo.id,
                otherUserName = message.senderInfo.name,
                otherUserProfilePicture = message.senderInfo.profilePic,
                sharedFileBundle = null,
                headerId = "",
                cameFromLinkInOtherChat = true
            )
        }
    }

    override fun onLongClick(v: View?): Boolean {
//        val popUpMenu = PopupMenu(context, v)
//        popUpMenu.inflate(R.menu.menu_chat_clipboard)
//
//        popUpMenu.menu.findItem(R.id.action_save_to_gallery).isVisible = false
//        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
//        popUpMenu.menu.findItem(R.id.action_delete).isVisible = type == MessageFlowType.OUT
//        popUpMenu.menu.findItem(R.id.action_message_info).isVisible =
//            type == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE
//
//
//        popUpMenu.setOnMenuItemClickListener(this)
//        popUpMenu.show()
        if(!(oneToOneChatViewModel.getSelectEnable() == true || groupChatViewModel.getSelectEnable() == true)) {
            if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                oneToOneChatViewModel.makeSelectEnable(true)
                oneToOneChatViewModel.selectChatMessage(message, true)
            } else if (messageType == MessageType.GROUP_MESSAGE) {
                frameLayoutRoot?.foreground =
                    resources.getDrawable(R.drawable.selected_chat_foreground)
                groupChatViewModel.makeSelectEnable(true)
                groupChatViewModel.selectChatMessage(message, true)
            }
        }

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> { }
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
        }
        return true
    }

    private fun viewMessageInfo() {
        navigation.navigateTo("chats/messageInfo",
            bundleOf(
                GroupMessageViewInfoFragment.INTENT_EXTRA_GROUP_ID to message.groupId,
                GroupMessageViewInfoFragment.INTENT_EXTRA_MESSAGE_ID to message.id
            )
        )
    }


    private fun deleteMessage() {
        if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
            oneToOneChatViewModel.deleteMessage(
                message.id
            )
        } else if (messageType == MessageType.GROUP_MESSAGE) {
            groupChatViewModel.deleteMessage(
                message.id
            )
        }
    }

    override fun getCurrentChatMessageOrThrow(): ChatMessage {
        return message
    }
}


class InLocationMessageView(
    context: Context,
    attrs: AttributeSet?
) : LocationMessageView(
    MessageFlowType.IN,
    MessageType.ONE_TO_ONE_MESSAGE,
    context,
    attrs
)

class OutLocationMessageView(
    context: Context,
    attrs: AttributeSet?
) : LocationMessageView(
    MessageFlowType.OUT,
    MessageType.ONE_TO_ONE_MESSAGE,
    context,
    attrs
)


class GroupInLocationMessageView(
    context: Context,
    attrs: AttributeSet?
) : LocationMessageView(
    MessageFlowType.IN,
    MessageType.GROUP_MESSAGE,
    context,
    attrs
)

class GroupOutLocationMessageView(
    context: Context,
    attrs: AttributeSet?
) : LocationMessageView(
    MessageFlowType.OUT,
    MessageType.GROUP_MESSAGE,
    context,
    attrs
)
