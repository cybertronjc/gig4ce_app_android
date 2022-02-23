package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.webkit.MimeTypeMap
import android.widget.RelativeLayout
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.IMediaMessage
import com.gigforce.core.IViewHolder
import com.gigforce.core.documentFileHelper.DocumentPrefHelper
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.modules.feature_chat.models.ChatMessageWrapper
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
abstract class MediaMessage(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    BaseChatMessageItemView {

    @Inject
    lateinit var documentPrefHelper: DocumentPrefHelper

    private val chatFileManager: ChatFileManager by lazy {
        ChatFileManager(
            context
        )
    }

    protected lateinit var message: ChatMessage
    protected lateinit var oneToOneChatViewModel: ChatPageViewModel
    protected lateinit var groupChatViewModel: GroupChatViewModel

    var iMediaMessage: IMediaMessage? = null
    var lifeCycleOwner: LifecycleOwner? = null


    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private var downloadAttachmentService: DownloadChatAttachmentService =
        RetrofitFactory.createService(DownloadChatAttachmentService::class.java)

        suspend fun downloadMediaFile(): Uri {
        iMediaMessage?.let {

            val downloadLink =
                storage.reference.child(it.attachmentPath!!).getDownloadUrlOrThrow()
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

            return fileRef.toUri()
        }

        throw NullPointerException("No Attachment Path found")
    }

    fun downloadMediaFileUsingFirebase() {
    }

    override fun bind(data: Any?) {

        val dataAndViewModels = data as ChatMessageWrapper
        message = dataAndViewModels.message
        groupChatViewModel = dataAndViewModels.groupChatViewModel
        oneToOneChatViewModel = dataAndViewModels.oneToOneChatViewModel
        lifeCycleOwner = dataAndViewModels.lifeCycleOwner
        iMediaMessage = message
        this.onBind(message)
    }

    abstract fun onBind(msg: ChatMessage)

    fun getFilePathRef(): File {
        return when (iMediaMessage?.type) {
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> chatFileManager.imageFilesDirectory
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> chatFileManager.videoFilesDirectory
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> chatFileManager.documentFilesDirectory
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> chatFileManager.audioFilesDirectory
            else -> chatFileManager.otherFilesDirectory
        }
    }


    fun returnFileIfAlreadyDownloadedElseNull(): Uri? {
        iMediaMessage?.let {
            val attachmentPath = it.attachmentPath ?: return null
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val file = File(getFilePathRef(), fileName)
            return if (file.exists()) file.toUri() else null

        }
        return null
    }

    private fun getMimeTypeFromFileName(
        fullFileNameWithExtension: String
    ): String {
        val extension = fullFileNameWithExtension.substringAfterLast(".")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)!!
    }
}