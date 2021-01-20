package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.os.Environment
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.file.FileUtils
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.modules.feature_chat.DownloadCompleted
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatMessage
import com.gigforce.modules.feature_chat.models.IMediaMessage
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException

abstract class MediaMessage(
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(
        context,
        attrs
), IViewHolder {

    private val refToGigForceAttachmentDirectory: File = Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!
    private var imagesDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)
    private var videosDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)
    private var documentsDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    var iMediaMessage:IMediaMessage? = null

    private var downloadAttachmentService: DownloadChatAttachmentService = RetrofitFactory.createService(DownloadChatAttachmentService::class.java)

   /* suspend fun downloadMediaFile(){
        iMediaMessage?.attachmentPath ?. let {

            val downloadLink = it

            val dirRef = getFilePathRef()
            if(!dirRef.exists()) dirRef.mkdirs()

            val fileName: String = FirebaseUtils.extractFilePath(downloadLink)
            val fileRef = File(dirRef, fileName)

            // download file from Server
            val response = downloadAttachmentService.downloadAttachment(downloadLink)

            if(response.isSuccessful){
                val body = response.body()!!
                FileUtils.writeResponseBodyToDisk(body, fileRef)
                // download completed, change the state
            }else{
                throw Exception("Unable to download attachment")
            }
        }
    }*/

    fun downloadMediaFileUsingFirebase(){

    }

    override fun bind(data: Any?) {
        iMediaMessage = data as IMediaMessage?
        this.onBind(data as ChatMessage)
    }

    abstract fun onBind(msg:ChatMessage)

    fun getFilePathRef():File{
        return when(iMediaMessage?.type){
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> imagesDirectoryRef
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> videosDirectoryRef
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> documentsDirectoryRef
            else -> throw java.lang.IllegalArgumentException()
        }
    }

    fun returnFileIfAlreadyDownloadedElseNull(): File?
    {
        iMediaMessage?.let {
            it.attachmentPath?.let {
                val fileName: String = FirebaseUtils.extractFilePath(it)
                val file = File(getFilePathRef(), fileName)
                return if(file.exists()) file else null
            }
            throw NullPointerException("attachment Path can not be null")
        }
        return  null
    }
}