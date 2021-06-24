package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.firebase.storage.FirebaseStorage


abstract class LocationMessageView(
        val type: MessageFlowType,
        val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder,
        View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    private lateinit var imageView: ImageView
    private lateinit var locationAddressTV: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var receivedStatusIV: ImageView
    private lateinit var senderNameTV: TextView

//    @Inject
//    lateinit var navigation: IChatNavigation


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

//        (this.context.applicationContext as ChatModuleProvider)
//            .provideChatModule()
//            .inject(this)
//        navigation.context = context
    }

    private fun findViews() {

        senderNameTV = this.findViewById(R.id.user_name_tv)
        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        locationAddressTV = this.findViewById(R.id.location_address_tv)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
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
    }

    private fun loadThumbnail(msg: ChatMessage) {
        if (msg.thumbnailBitmap != null) {

            Glide.with(context)
                    .load(msg.thumbnailBitmap)
                    .placeholder(getCircularProgressDrawable())
                    .into(imageView)
        } else if (msg.thumbnail != null) {

            val thumbnailStorageRef = firebaseStorage.reference.child(msg.thumbnail!!)
            Glide.with(context)
                    .load(thumbnailStorageRef)
                    .placeholder(getCircularProgressDrawable())
                    .into(imageView)
        } else if (msg.attachmentPath != null) {
            val thumbnailStorageRef = firebaseStorage.reference.child(msg.attachmentPath!!)
            Glide.with(context)
                    .load(thumbnailStorageRef)
                    .placeholder(getCircularProgressDrawable())
                    .into(imageView)
        }
    }

    override fun bind(data: Any?) {
        data?.let {
            val dataAndViewModels =  it as ChatMessageWrapper
            message = dataAndViewModels.message
            groupChatViewModel = dataAndViewModels.groupChatViewModel
            oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

            senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && type == MessageFlowType.IN
            senderNameTV.text = message.senderInfo.name

            locationAddressTV.setText("\uD83D\uDCCD ${message.locationPhysicalAddress}")
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

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    private fun setReceivedStatus(msg: ChatMessage) = when (msg.status) {
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


    override fun onClick(v: View?) {
        //Launch Map
        if (message == null)
            return

        val lat = message?.location?.latitude ?: 0.0
        val long = message?.location?.longitude ?: 0.0

        if (lat != 0.0) {
            val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Location)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No App found to open location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val popUpMenu = PopupMenu(context, v)
        popUpMenu.inflate(R.menu.menu_chat_clipboard)

        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
        popUpMenu.menu.findItem(R.id.action_delete).isVisible = messageType == MessageType.GROUP_MESSAGE &&  type == MessageFlowType.OUT

        popUpMenu.setOnMenuItemClickListener(this)
        popUpMenu.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> {}
            R.id.action_delete -> deleteMessage()
        }
        return true
    }

    private fun deleteMessage() {
        if (messageType == MessageType.ONE_TO_ONE_MESSAGE) {
            //
        } else if (messageType == MessageType.GROUP_MESSAGE) {
            groupChatViewModel.deleteMessage(
                message.id
            )
        }
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