package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


abstract class DocumentMessageView(
        private val flowType: MessageFlowType,
        private val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(context, attrs),
        IViewHolder, View.OnClickListener {

    //Views
    private lateinit var linearLayout: ConstraintLayout
    private lateinit var textView: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var progressbar: View
    private lateinit var receivedStatusIV: ImageView

    init {
        setDefault()
        inflate()
        findViews()
        cardView.setOnClickListener(this)
    }

    private fun findViews() {
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

        if (msg.attachmentPath.isNullOrBlank()) {
            handleDocumentUploading()
        } else {

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                progressbar.gone()
            } else {
                if (msg.attachmentCurrentlyBeingDownloaded) {
                    progressbar.visible()
                } else {
                    progressbar.gone()
                }
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
                            "com.gigforce.app.provider",
                            file
                    ), "application/pdf"
            )
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(this)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadAttachment() = GlobalScope.launch {

        this.launch(Dispatchers.Main) {
            handleDownloadInProgress()
        }

        try {
            val file = downloadMediaFile()
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