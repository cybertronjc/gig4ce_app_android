package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.os.Environment
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.core.ChatConstants
import java.io.File

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


    fun returnFileIfAlreadyDownloadedElseNull(
            type: String,
            attachmentPathOnServer: String): File? {
        val fileName: String = FirebaseUtils.extractFilePath(attachmentPathOnServer)

        if (type == ChatConstants.ATTACHMENT_TYPE_IMAGE) {
            val file = File(imagesDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        } else if (type == ChatConstants.ATTACHMENT_TYPE_VIDEO) {
            val file = File(videosDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        } else if (type == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
            val file = File(documentsDirectoryRef, fileName)
            return if (file.exists())
                file
            else
                null
        }

        throw IllegalArgumentException("other types not supported yet")
    }


}