package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.screens.GroupMessageViewInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
abstract class DocumentMessageView(
        private val flowType: MessageFlowType,
        private val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(context, attrs),
        IViewHolder,
    View.OnClickListener,
    View.OnLongClickListener,
    PopupMenu.OnMenuItemClickListener{

    @Inject
    lateinit var navigation: INavigation

    //Views
    private lateinit var linearLayout: ConstraintLayout
    private lateinit var senderNameTV: TextView
    private lateinit var textView: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var progressbar: View
    private lateinit var receivedStatusIV: ImageView

    //Data
    private lateinit var chatMessage : ChatMessage

    init {
        setDefault()
        inflate()
        findViews()
        cardView.setOnClickListener(this)
        cardView.setOnLongClickListener(this)
    }

    private fun findViews() {
        senderNameTV = this.findViewById(R.id.user_name_tv)
        linearLayout = this.findViewById(R.id.ll_msgContainer)
        textView = this.findViewById(R.id.tv_file_name)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        progressbar = this.findViewById(R.id.progress)
        receivedStatusIV = this.findViewById(R.id.tv_received_status)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    override fun onBind(msg: ChatMessage) {
        chatMessage = msg

        senderNameTV.isVisible = messageType == MessageType.GROUP_MESSAGE && flowType == MessageFlowType.IN
        senderNameTV.text = msg.senderInfo.name

        if (msg.attachmentPath.isNullOrBlank()) {
            handleDocumentUploading()
        } else {

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                progressbar.gone()
            } else {
//                if (msg.attachmentCurrentlyBeingDownloaded) {
//                    progressbar.visible()
//                } else {
//                    progressbar.gone()
//                }
            }
        }


        textViewTime.text = msg.timestamp?.toDisplayText()
        textView.text = msg.attachmentName

        when (msg.flowType) {
            "out" -> {
                setReceivedStatus(msg)
            }
        }
    }

    private fun handleDocumentUploading() {
        progressbar.gone()
    }

    fun inflate() {
        if (flowType == MessageFlowType.IN) {
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_document_in, this, true)
        } else
            LayoutInflater.from(context).inflate(R.layout.recycler_item_chat_text_with_document_out, this, true)
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
        val file = returnFileIfAlreadyDownloadedElseNull()

        if (file != null) {
            openDocument(file)
        } else {
            downloadAttachment()
        }
    }


    private fun openDocument(file: File) {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                    FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                    ), getMimeType(Uri.fromFile(file))
            )
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(this)
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.unable_to_open_chat), Toast.LENGTH_SHORT).show()
            }
        }
    }

    open fun getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            val cr: ContentResolver = context.getContentResolver()
            cr.getType(uri)
        } else {
            val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        }
        return mimeType
    }

    private fun downloadAttachment() = GlobalScope.launch {

        this.launch(Dispatchers.Main) {
            handleDownloadInProgress()
        }

        try {
            downloadMediaFile()
            this.launch(Dispatchers.Main) {
                handleDownloadedCompleted()
            }
        } catch (e: Exception) {
        }
    }

    private fun handleDownloadInProgress() {
        progressbar.visible()
    }

    private fun handleDownloadedCompleted() {
        progressbar.gone()
    }

    override fun onLongClick(v: View?): Boolean {
        val popUpMenu = PopupMenu(context, v)
        popUpMenu.inflate(R.menu.menu_chat_clipboard)

        popUpMenu.menu.findItem(R.id.action_copy).isVisible = false
        popUpMenu.menu.findItem(R.id.action_delete).isVisible =  flowType == MessageFlowType.OUT
        popUpMenu.menu.findItem(R.id.action_message_info).isVisible =  flowType == MessageFlowType.OUT && messageType == MessageType.GROUP_MESSAGE

        popUpMenu.setOnMenuItemClickListener(this)
        popUpMenu.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val itemClicked = item ?: return true

        when (itemClicked.itemId) {
            R.id.action_copy -> {}
            R.id.action_delete -> deleteMessage()
            R.id.action_message_info -> viewMessageInfo()
        }
        return true
    }

    private fun viewMessageInfo() {
        navigation.navigateTo("chats/messageInfo",
            bundleOf(
                GroupMessageViewInfoFragment.INTENT_EXTRA_GROUP_ID to chatMessage.groupId,
                GroupMessageViewInfoFragment.INTENT_EXTRA_MESSAGE_ID to chatMessage.id
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



class InOneToOneDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.IN,
        messageType = MessageType.ONE_TO_ONE_MESSAGE,
        context = context,
        attrs = attrs
)

class OutOneToOneDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.OUT,
        messageType = MessageType.ONE_TO_ONE_MESSAGE,
        context = context,
        attrs = attrs
)

class GroupInDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.IN,
        messageType = MessageType.GROUP_MESSAGE,
        context = context,
        attrs = attrs
)

class GroupOutDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.OUT,
        messageType = MessageType.GROUP_MESSAGE,
        context = context,
        attrs = attrs
)