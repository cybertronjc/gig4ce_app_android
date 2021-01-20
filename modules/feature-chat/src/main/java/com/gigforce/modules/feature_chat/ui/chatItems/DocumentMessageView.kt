package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessage


abstract class DocumentMessageView(
        private val flowType: MessageFlowType,
        private val messageType: MessageType,
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(context, attrs),
        IViewHolder {

    //Views
    private lateinit var linearLayout: ConstraintLayout
    private lateinit var textView: TextView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var progressbar: View

    init {
        setDefault()
        inflate()
        findViews()
    }

    private fun findViews() {
        linearLayout = this.findViewById(R.id.ll_msgContainer)
        textView = this.findViewById(R.id.tv_file_name)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        progressbar = this.findViewById(R.id.progress)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        if (flowType == MessageFlowType.IN) {
            LayoutInflater.from(context).inflate(R.layout.item_chat_text_with_document, this, true)
        } else
            LayoutInflater.from(context).inflate(R.layout.item_chat_text_with_document, this, true)
    }

    override fun bind(data: Any?) {
        val msg = data as ChatMessage? ?: return

        if (msg.attachmentPath.isNullOrBlank()) {
            progressbar.visible()
        } else {
            progressbar.gone()

            val fileName: String = FirebaseUtils.extractFilePath(msg.attachmentPath!!)
            val downloadedFile = null
//                    returnFileIfAlreadyDownloadedElseNull(
//                            ChatConstants.ATTACHMENT_TYPE_DOCUMENT,
//                            fileName
//                    )
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

        textView.text = msg.attachmentName
        when (msg.flowType) {
            "in" -> {
                textViewTime.text = msg.timestamp?.toDisplayText()
                textViewTime.setTextColor(Color.parseColor("#979797"))
                linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                textView.setTextColor(Color.parseColor("#000000"))

                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(26, 5, 160, 5)
                cardView.layoutParams = layoutParams
            }
            "out" -> {
                textViewTime.text = msg.timestamp?.toDisplayText()
                linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                textView.setTextColor(Color.parseColor("#ffffff"))
                textViewTime.setTextColor(Color.parseColor("#ffffff"))

                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.END
                layoutParams.setMargins(160, 5, 26, 5)
                cardView.layoutParams = layoutParams
            }
        }

    }
}

class GroupOneToOneDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.IN,
        messageType = MessageType.GROUP_MESSAGE,
        context = context,
        attrs = attrs
)

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

class OutGroupDocumentMessageView(
        context: Context,
        attrs: AttributeSet?
) : DocumentMessageView(
        flowType = MessageFlowType.OUT,
        messageType = MessageType.GROUP_MESSAGE,
        context = context,
        attrs = attrs
)