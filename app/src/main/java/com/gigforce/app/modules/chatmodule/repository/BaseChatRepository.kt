package com.gigforce.app.modules.chatmodule.repository

import android.content.Context
import android.net.Uri
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
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

    suspend fun uploadChatAttachment(fileNameWithExtension: String, image: Uri) =
        suspendCoroutine<String> { cont ->
            val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(fileNameWithExtension)

            filePathOnServer
                .putFile(image)
                .addOnSuccessListener {
                    filePathOnServer
                        .downloadUrl
                        .addOnSuccessListener {
                            cont.resume(it.toString())

                        }.addOnFailureListener {
                            cont.resumeWithException(it)
                        }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    suspend fun uploadChatAttachment(fileNameWithExtension: String, data: ByteArray) =
        suspendCoroutine<String> { cont ->
            val filePathOnServer = firebaseStorage.reference
                .child("chat_attachments")
                .child(fileNameWithExtension)

            filePathOnServer
                .putBytes(data)
                .addOnSuccessListener {
                    filePathOnServer
                        .downloadUrl
                        .addOnSuccessListener {
                            cont.resume(it.toString())

                        }.addOnFailureListener {
                            cont.resumeWithException(it)
                        }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

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


}