package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.modules.feature_chat.R

interface BaseChatMessageItemView {

    fun getCurrentChatMessageOrThrow() : ChatMessage

    fun setQuotedMessageOnView(
        context : Context,
        firebaseAuthStateListener: FirebaseAuthStateListener,
        type : MessageFlowType,
        chatMessage: ChatMessage,
        quotedMessagePreviewContainer : LinearLayout
    ) {
        if (chatMessage.isAReplyToOtherMessage && chatMessage.replyForMessage != null) {
            quotedMessagePreviewContainer.visible()
            val replyMessage = chatMessage.replyForMessage!!
            quotedMessagePreviewContainer.removeAllViews()


            val replyView = if (type == MessageFlowType.OUT) {
                LayoutInflater.from(context).inflate(
                    R.layout.layout_reply_to_layout_view_out,
                    null,
                    false
                )
            } else {
                LayoutInflater.from(context).inflate(
                    R.layout.layout_reply_to_layout_view_in,
                    null,
                    false
                )
            }
            quotedMessagePreviewContainer.addView(replyView)

            //Setting common vars and listeners
            val senderNameTV: TextView = replyView.findViewById(R.id.user_name_tv)
            val messageTV: TextView = replyView.findViewById(R.id.tv_msgValue)
            val messageImageIV: GigforceImageView = replyView.findViewById(R.id.message_image)

            senderNameTV.text =
                if (replyMessage.senderInfo.id == firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid)
                    "You"
                else
                    replyMessage.senderInfo.name

            when (replyMessage.type) {
                ChatConstants.MESSAGE_TYPE_TEXT -> {
                    messageTV.text = replyMessage.content
                    messageImageIV.gone()
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {
                    messageTV.text = replyMessage.attachmentName
                    messageImageIV.visible()

                    if (replyMessage.thumbnailBitmap != null) {
                        messageImageIV.loadImage(replyMessage.thumbnailBitmap!!, true)
                    } else if (replyMessage.thumbnail != null) {
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(
                            replyMessage.thumbnail!!,
                            -1,
                            -1,
                            true
                        )
                    } else if (replyMessage.attachmentPath != null) {
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(
                            replyMessage.attachmentPath!!,
                            -1,
                            -1,
                            true
                        )
                    } else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {
                    messageTV.text = replyMessage.attachmentName
                    messageImageIV.visible()

                    if (replyMessage.thumbnailBitmap != null) {
                        messageImageIV.loadImage(replyMessage.thumbnailBitmap!!, true)
                    } else if (replyMessage.thumbnail != null) {
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(
                            replyMessage.thumbnail!!,
                            -1,
                            -1,
                            true
                        )
                    } else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> {
                    messageTV.text = replyMessage.locationPhysicalAddress
                    messageImageIV.visible()

                    if (replyMessage.thumbnailBitmap != null) {
                        messageImageIV.loadImage(replyMessage.thumbnailBitmap!!, true)
                    } else if (replyMessage.thumbnail != null) {
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(
                            replyMessage.thumbnail!!,
                            -1,
                            -1,
                            true
                        )
                    } else if (replyMessage.attachmentPath != null) {
                        messageImageIV.loadImageIfUrlElseTryFirebaseStorage(
                            replyMessage.attachmentPath!!,
                            -1,
                            -1,
                            true
                        )
                    } else {
                        //load default image
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                    messageTV.text = replyMessage.attachmentName
                    messageImageIV.visible()
                    messageImageIV.loadImage(R.drawable.ic_document_background, true)
                }
                else -> {
                }
            }

        } else {
            quotedMessagePreviewContainer.gone()
            quotedMessagePreviewContainer.removeAllViews()
        }
    }
}