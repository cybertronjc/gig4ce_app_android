package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.os.Environment
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatMessage
import com.gigforce.modules.feature_chat.models.IMediaMessage
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

    fun downloadMediaFile(){
        //todo: implement
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