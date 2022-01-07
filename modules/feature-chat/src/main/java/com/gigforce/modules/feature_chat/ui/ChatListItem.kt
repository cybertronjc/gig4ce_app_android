package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.TextDrawable
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.chat.models.ChatHeader
import com.gigforce.common_ui.chat.models.ChatListItemDataObject
import com.gigforce.common_ui.chat.models.ChatListItemDataWrapper
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.recycler_item_contact.view.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatListItem(
        context: Context
) :
        RelativeLayout(context),
        IViewHolder,
        View.OnClickListener, View.OnLongClickListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    //Views
    private lateinit var contextImageView: GigforceImageView
    private lateinit var textViewName: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var textViewTime: TextView
    private lateinit var lastMessageType: ImageView
    private lateinit var unseenMessageCountIV: ImageView
    private lateinit var userOnlineIV: ImageView
    private lateinit var statusIV: ImageView
    private lateinit var viewModel: ChatHeadersViewModel
    private lateinit var selectUnselectIcon: ImageView

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    init {

        LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_list, this, true)
        this.findViewById<View>(R.id.contactItemRoot).setOnClickListener(this)
        this.findViewById<View>(R.id.contactItemRoot).setOnLongClickListener(this)

        findViews()
    }

    private fun findViews() {

        contextImageView = this.findViewById(R.id.iv_profile)
        textViewName = this.findViewById(R.id.txt_title)
        txtSubtitle = this.findViewById(R.id.txt_subtitle)
        textViewTime = this.findViewById(R.id.txt_time)
        userOnlineIV = this.findViewById(R.id.user_online_iv)
        lastMessageType = this.findViewById(R.id.last_message_type_iv)
        unseenMessageCountIV = this.findViewById(R.id.unseen_msg_count_iv)
        statusIV = this.findViewById(R.id.tv_received_status)
        selectUnselectIcon = this.findViewById(R.id.select_unselect_icon)
    }

    private var dObj: ChatListItemDataObject? = null

    override fun bind(data: Any?) {
        dObj = null
        data?.let {
            val dataAndSharedData = data as ChatListItemDataWrapper
            viewModel = dataAndSharedData.viewModel
            var searchText = dataAndSharedData.searchText
            dObj = dataAndSharedData.chatItem
            dObj?.let { chatHeader ->

                userOnlineIV.isVisible = chatHeader.isOtherUserOnline
                statusIV.isVisible = chatHeader.lastMsgFlowType == ChatConstants.FLOW_TYPE_OUT && chatHeader.chatType == ChatConstants.CHAT_TYPE_USER


                if (chatHeader.unreadCount != 0) {
                    val drawable = TextDrawable.builder().buildRound(
                            chatHeader.unreadCount.toString(),
                            ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                    )
                    unseenMessageCountIV.setImageDrawable(drawable)
//                    textViewName.setTextColor(
//                            ResourcesCompat.getColor(context.resources,
//                                    R.color.lipstick,
//                                    null
//                            )
//                    )
                    //txtSubtitle.setTypeface(null, Typeface.BOLD);
                } else {
//                    textViewName.setTextColor(
//                            ResourcesCompat.getColor(context.resources,
//                                    R.color.dove_grey,
//                                    null
//                            )
//                    )
                    unseenMessageCountIV.setImageDrawable(null)
                    //txtSubtitle.setTypeface(null, Typeface.NORMAL);
                }

                if (chatHeader.type == ChatConstants.CHAT_TYPE_USER) {

                    //textViewName.text = chatHeader.title
                    val dataString = chatHeader.title
                    if (searchText != null && !searchText.isEmpty()) {
                        val startPos: Int = dataString.toString().toLowerCase()?.indexOf(searchText.toLowerCase())
                        val endPos: Int = startPos + searchText.length
                        if (startPos != -1) {
                            val spannable: Spannable = SpannableString(dataString.toString())
                            val colorStateList =
                                ColorStateList(arrayOf(intArrayOf()), intArrayOf(context.getColor(R.color.colorPrimary)))
                            val textAppearanceSpan =
                                TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                            spannable.setSpan(
                                textAppearanceSpan,
                                startPos,
                                endPos,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            textViewName.text = spannable

                        } else {
                            textViewName.text = chatHeader.title
                        }
                    } else {
                        textViewName.text = chatHeader.title
                    }
                    if (chatHeader.profilePath.isNotBlank()) {

                        if (Patterns.WEB_URL.matcher(chatHeader.profilePath).matches()) {

                            Glide.with(context)
                                    .load(Uri.parse(chatHeader.profilePath))
                                    .placeholder(R.drawable.ic_user_2)
                                    .into(contextImageView)
                        } else {

                            val profilePathRef = if (chatHeader.profilePath.startsWith("profile_pics/"))
                                storage.reference.child(chatHeader.profilePath)
                            else
                                storage.reference.child("profile_pics/${chatHeader.profilePath}")

                            Glide.with(context)
                                    .load(profilePathRef)
                                    .placeholder(R.drawable.ic_user_2)
                                    .into(contextImageView)
                        }
                    } else {

                        Glide.with(context).load(R.drawable.ic_user_2).into(contextImageView)
                    }

                } else if (chatHeader.type == ChatConstants.CHAT_TYPE_GROUP) {

                    //textViewName.text = chatHeader.groupName
                    val dataString = chatHeader.groupName
                    if (searchText != null && !searchText.isEmpty()) {
                        val startPos: Int = dataString.toString().toLowerCase()?.indexOf(searchText.toLowerCase())
                        val endPos: Int = startPos + searchText.length
                        if (startPos != -1) {
                            val spannable: Spannable = SpannableString(dataString.toString())
                            val colorStateList =
                                ColorStateList(arrayOf(intArrayOf()), intArrayOf(context.getColor(R.color.colorPrimary)))
                            val textAppearanceSpan =
                                TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null)
                            spannable.setSpan(
                                textAppearanceSpan,
                                startPos,
                                endPos,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            textViewName.text = spannable

                        } else {
                            textViewName.text = chatHeader.groupName
                        }
                    } else {
                        textViewName.text = chatHeader.groupName
                    }
                    val userAvatarUrl = chatHeader.groupAvatar
                    if (userAvatarUrl.isBlank()) {

                        //Show Default User avatar
                        Glide.with(context).load(R.drawable.ic_create_new_group).into(contextImageView)
                    } else {

                        contextImageView.loadImageIfUrlElseTryFirebaseStorage(
                            userAvatarUrl,
                            R.drawable.ic_group,
                            R.drawable.ic_group
                        )
                    }
                }

                if(chatHeader.lastMessageDeleted){
                    lastMessageType.visible()
                    lastMessageType.setImageResource(R.drawable.ic_delete_forever_12)
                    txtSubtitle.text = context.getString(R.string.message_deleted_chat)
                    txtSubtitle.setTypeface(null,Typeface.ITALIC)
                } else {

                    txtSubtitle.setTypeface(null,Typeface.NORMAL)
                    val messagePrefix = if (chatHeader.senderName.isNotBlank()) "${chatHeader.senderName} :" else ""
                    when (chatHeader.lastMessageType) {
                        ChatConstants.MESSAGE_TYPE_TEXT -> {
                            lastMessageType.gone()
                            txtSubtitle.text = "$messagePrefix ${chatHeader.subtitle}"
                        }
                        ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                            lastMessageType.visible()
                            lastMessageType.setImageResource(R.drawable.ic_chat_video_2)
                            txtSubtitle.text = messagePrefix + context.getString(R.string.video_chat)
                        }
                        ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> {
                            lastMessageType.visible()
                            lastMessageType.setImageResource(R.drawable.ic_baseline_mic_24)
                            txtSubtitle.text = messagePrefix + context.getString(R.string.audio_chat)
                        }
                        ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                            lastMessageType.visible()
                            lastMessageType.setImageResource(R.drawable.ic_chat_document_2)
                            txtSubtitle.text = messagePrefix + context.getString(R.string.document_chat)
                        }
                        ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                            lastMessageType.visible()
                            lastMessageType.setImageResource(R.drawable.ic_chat_image_2)
                            txtSubtitle.text = messagePrefix + context.getString(R.string.image_chat)
                        }
                        ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                            lastMessageType.visible()
                            lastMessageType.setImageResource(R.drawable.ic_chat_location_2)
                            txtSubtitle.text = messagePrefix + context.getString(R.string.location_chat)
                        }
                        else -> {
                            lastMessageType.gone()
                            txtSubtitle.text = ""
                        }
                    }
                }
                if (chatHeader.unreadCount != 0){
                    txtSubtitle.setTypeface(null, Typeface.BOLD)
                    txtSubtitle.setTextColor(resources.getColor(R.color.subtitle_black_text_color))
                } else{
                    txtSubtitle.setTypeface(null, Typeface.NORMAL)
                    txtSubtitle.setTextColor(resources.getColor(R.color.gray_text_color))
                }

                textViewTime.text = chatHeader.timeDisplay
                setReceivedStatus(chatHeader.status)
                setSelectEnable(chatHeader, viewModel)
            }
        }
    }

    private fun setSelectEnable(
        chatHeader: ChatListItemDataObject,
        viewModel: ChatHeadersViewModel
    ) {
        if (viewModel.isMultiSelectEnable() == true){
            Log.d("multiselect", "enable")
            selectUnselectIcon.visible()
            if (viewModel.isChatSelected(chatHeader = chatHeader)){
                selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_selected))
            }else{
                selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_unselected))
            }

        }else{
            selectUnselectIcon.gone()
        }
    }

    private fun setReceivedStatus(status: Int) = when (status) {
        ChatConstants.MESSAGE_STATUS_NOT_SENT -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_DELIVERED_TO_SERVER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_sent_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_RECEIVED_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_delivered_grey)
                    .into(statusIV)
        }
        ChatConstants.MESSAGE_STATUS_READ_BY_USER -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_seen)
                    .into(statusIV)
        }
        else -> {
            Glide.with(context)
                    .load(R.drawable.ic_msg_pending)
                    .into(statusIV)
        }
    }

    override fun onClick(v: View?) {
        dObj?.let {

            if (viewModel.isMultiSelectEnable() == true){
                //select/unselect
                if (dObj?.let { viewModel.isChatSelected(it) } == true){
                    selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_unselected))
                    viewModel.addOrRemoveChatFromSelectedList(it)
                }else{
                    selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_selected))
                    dObj?.let { viewModel.addOrRemoveChatFromSelectedList(it) }
                }
            }else{
                chatNavigation.navigateToChatPage(
                    chatType = it.chatType,
                    otherUserId = it.profileId,
                    headerId = it.id,
                    otherUserName = it.title,
                    otherUserProfilePicture = it.profilePath,
                    sharedFileBundle = viewModel.sharedFiles
                )

                viewModel.sharedFiles = null
            }


        }
    }

    override fun onLongClick(v: View?): Boolean {
        selectUnselectIcon.visible()
        viewModel.setMultiSelectEnable(true)
        if (dObj?.let { viewModel.isChatSelected(it) } == true){
            selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_unselected))
            dObj?.let { viewModel.addOrRemoveChatFromSelectedList(it) }
            return false
        }else{
            selectUnselectIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_selected))
            dObj?.let { viewModel.addOrRemoveChatFromSelectedList(it) }
        }
        return true
    }
}