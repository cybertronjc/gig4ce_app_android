package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.net.toFile
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.IMediaMessage
import com.gigforce.core.extensions.getFileOrThrow
import com.gigforce.core.extensions.putFileOrThrow
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.File

abstract class MediaMessage(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    BaseChatMessageItemView{

    private val refToGigForceAttachmentDirectory: File = Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!

    private var imagesDirectoryRef: File =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)!!
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
    } else {
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
    }

    private var videosDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)
    private var documentsDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    protected lateinit var message: ChatMessage
    protected lateinit var oneToOneChatViewModel: ChatPageViewModel
    protected lateinit var groupChatViewModel: GroupChatViewModel

    var iMediaMessage: IMediaMessage? = null


    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private var downloadAttachmentService: DownloadChatAttachmentService =
        RetrofitFactory.createService(DownloadChatAttachmentService::class.java)

    suspend fun downloadMediaFile(): File {
        iMediaMessage?.let {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, it.attachmentName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                context.contentResolver.run {
                    val uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@run
                    val outputStreamToDestFile = openOutputStream(uri) ?: return@run

                    val downloadLink = storage.reference.child(it.attachmentPath!!).getDownloadUrlOrThrow()
                    val fullDownloadLink = downloadLink.toString()

                    // download file from Server
                    val response = downloadAttachmentService.downloadAttachment(fullDownloadLink)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        if (!FileUtils.writeResponseBodyToDisk(body, outputStreamToDestFile)) {
                            throw Exception("Unable to save downloaded chat attachment")
                        }
                    } else {
                        throw Exception("Unable to download attachment")
                    }

                    return uri.toFile()
                }
            } else {
                val downloadLink = storage.reference.child(it.attachmentPath!!).getDownloadUrlOrThrow()
                val fullDownloadLink = downloadLink.toString()

                val dirRef = getFilePathRef()
                if (!dirRef.exists()) dirRef.mkdirs()

                val fileName: String = FirebaseUtils.extractFilePath(fullDownloadLink)
                val fileRef = File(dirRef, fileName)

                // download file from Server
                val response = downloadAttachmentService.downloadAttachment(fullDownloadLink)

                if (response.isSuccessful) {
                    val body = response.body()!!
                    if (!FileUtils.writeResponseBodyToDisk(body, fileRef)) {
                        throw Exception("Unable to save downloaded chat attachment")
                    }
                } else {
                    throw Exception("Unable to download attachment")
                }

                return fileRef
            }
        }

        throw NullPointerException("No Attachment Path found")
    }

    fun downloadMediaFileUsingFirebase() {

    }

    override fun bind(data: Any?) {

        val dataAndViewModels =  data as ChatMessageWrapper
        message = dataAndViewModels.message
        groupChatViewModel = dataAndViewModels.groupChatViewModel
        oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel

        iMediaMessage = message
        this.onBind(message)
    }

    abstract fun onBind(msg: ChatMessage)

    fun getFilePathRef(): File {
        return when (iMediaMessage?.type) {
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> imagesDirectoryRef
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> videosDirectoryRef
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> documentsDirectoryRef
            else -> throw java.lang.IllegalArgumentException()
        }
    }

    fun returnFileIfAlreadyDownloadedElseNull(): File? {
        iMediaMessage?.let {
            it.attachmentPath?.let {
                val fileName: String = FirebaseUtils.extractFilePath(it)
                val file = File(getFilePathRef(), fileName)
                return if (file.exists()) file else null
            }
            throw NullPointerException("attachment Path can not be null")
        }
        return null
    }
}