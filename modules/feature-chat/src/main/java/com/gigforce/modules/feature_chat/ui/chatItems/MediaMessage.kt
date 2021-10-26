package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.webkit.MimeTypeMap
import android.widget.RelativeLayout
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.common_ui.chat.models.IMediaMessage
import com.gigforce.core.IViewHolder
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.documentFileHelper.DocumentPrefHelper
import com.gigforce.core.documentFileHelper.openOutputStreamOrThrow
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
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

    private val refToGigForceAttachmentDirectory: File =
        Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!

    private var imagesDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
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

    suspend fun downloadMediaFile(): Uri {
        iMediaMessage?.let {

            if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

                val downloadLink =
                    storage.reference.child(it.attachmentPath!!).getDownloadUrlOrThrow()
                val fullDownloadLink = downloadLink.toString()

                val dirRef = getMediaFolderRef()
                val fileName: String = FirebaseUtils.extractFilePath(fullDownloadLink)

                if(dirRef.findFile(fileName) == null){

                    val response = downloadAttachmentService.downloadAttachment(fullDownloadLink)
                    if (response.isSuccessful) {
                        val mediaFile = dirRef.createFile(
                            getMimeTypeFromFileName(fileName),
                            fileName
                        ) ?: throw IllegalStateException("unable to create media file")

                        val outputStreamToMediaFile = mediaFile.openOutputStreamOrThrow(
                            context = context
                        )

                        val body = response.body()!!
                        if (!FileUtils.writeResponseBodyToDisk(body, outputStreamToMediaFile)) {
                            throw Exception("Unable to save downloaded chat attachment")
                        }

                        return mediaFile.uri
                    } else {
                        throw Exception("Unable to download attachment")
                    }
                }

            } else {
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

    fun getMediaFolderRef(): DocumentFile {
        val rootTreeUri = documentPrefHelper.getSavedDocumentTreeUri() ?: throw IllegalStateException("root tree uri not saved")
        val documentsTree = DocumentFile.fromTreeUri(context, rootTreeUri) ?: throw IllegalStateException("root tree uri not saved")

        var mediaFolder = when (iMediaMessage?.type) {
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> documentsTree.findFile(ChatConstants.DIRECTORY_IMAGES)
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> documentsTree.findFile(ChatConstants.DIRECTORY_VIDEOS)
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> documentsTree.findFile(ChatConstants.DIRECTORY_DOCUMENTS)
            else -> throw IllegalArgumentException()
        }

        if (mediaFolder == null) {

            mediaFolder = when (iMediaMessage?.type) {
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> documentsTree.createDirectory(
                    ChatConstants.DIRECTORY_IMAGES
                )
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> documentsTree.createDirectory(
                    ChatConstants.DIRECTORY_VIDEOS
                )
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> documentsTree.createDirectory(
                    ChatConstants.DIRECTORY_DOCUMENTS
                )
                else -> throw IllegalArgumentException()
            }
        }

        return mediaFolder!!
    }

    fun returnFileIfAlreadyDownloadedElseNull(): Uri? {
        iMediaMessage?.let {
            val attachmentPath = it.attachmentPath ?: return null
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMediaFolderRef().findFile(fileName)?.uri
            } else{
                val file = File(getFilePathRef(), fileName)
                if (file.exists()) file.toUri() else null
            }
        }
        return null
    }

    private fun getMimeTypeFromFileName(
        fullFileNameWithExtension : String
    ) : String{
       val extension =  fullFileNameWithExtension.substringAfterLast(".")
       return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)!!
    }
}