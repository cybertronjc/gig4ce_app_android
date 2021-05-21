package com.gigforce.common_ui.chat

import android.content.Context
import android.net.Uri
import com.gigforce.core.fb.BaseFirestoreDBRepository
//import com.gigforce.modules.feature_chat.core.ChatConstants
import com.google.firebase.storage.FirebaseStorage
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class BaseChatRepository constructor(
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : BaseFirestoreDBRepository() {

    private fun prepareUniqueImageName(fileNameWithExtension: String): String {
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        return "${getUID()}$timeStamp$fileNameWithExtension"
    }

    suspend fun transcodeVideo(context: Context, uri: Uri, dest: File) =
            suspendCancellableCoroutine<File> { cont ->

                val transcodeJob = Transcoder.into(dest.path)
                        .addDataSource(context, uri)
                        .setListener(object : TranscoderListener {
                            override fun onTranscodeCompleted(successCode: Int) {
                                cont.resume(dest)
                            }

                            override fun onTranscodeProgress(progress: Double) {}

                            override fun onTranscodeCanceled() {
                                cont.resumeWithException(CancellationException("Video Compresssion Cancelled"))
                            }

                            override fun onTranscodeFailed(exception: Throwable) {
                                cont.resumeWithException(exception)
                            }
                        }).transcode()

                cont.invokeOnCancellation {
                    if (!transcodeJob.isCancelled) {
                        transcodeJob.cancel(true)
                    }
                }
            }

    /**
     * Upload Chat Attachment to Chat Storage
     * and returns full path in storage
     */
    suspend fun uploadChatAttachment(
            fileNameWithExtension: String,
            file: Uri,
            headerId: String,
            isGroupChatMessage: Boolean,
            messageType: String
    ) = suspendCoroutine<String> { cont ->

        //Path - chat_attachments/{chat-type}/{headerId}/{type}/
        // 1. chat-type - group or one_to_one
        // 2. headerId
        // 3. type - Images,Videos etc

        val type = when (messageType) {
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> "Images"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> "Videos"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> "Documents"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> "Audios"
            else -> throw IllegalArgumentException("message type not supported yet")
        }

        val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(if (isGroupChatMessage) "group" else "one_to_one")
                .child(headerId)
                .child(type)
                .child(fileNameWithExtension)

        filePathOnServer
                .putFile(file)
                .addOnSuccessListener {
                    cont.resume(it.metadata?.path!!)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }

    /**
     * Upload Chat Attachment to Chat Storage
     * and returns full path in storage
     */
    suspend fun uploadChatAttachment(
            fileNameWithExtension: String,
            file: ByteArray,
            headerId: String,
            isGroupChatMessage: Boolean,
            messageType: String
    ) = suspendCoroutine<String> { cont ->

        //Path - chat_attachments/{chat-type}/{headerId}/{type}/
        // 1. chat-type - group or one_to_one
        // 2. headerId
        // 3. type - Images,Videos etc

        val type = when (messageType) {
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> "Images"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> "Videos"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> "Documents"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> "Audios"
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_LOCATION -> "Locations"
            else -> throw IllegalArgumentException("message type not supported yet")
        }

        val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(if (isGroupChatMessage) "group" else "one_to_one")
                .child(headerId)
                .child(type)
                .child(fileNameWithExtension)

        filePathOnServer
                .putBytes(file)
                .addOnSuccessListener {
                    cont.resume(it.metadata?.path!!)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }


}