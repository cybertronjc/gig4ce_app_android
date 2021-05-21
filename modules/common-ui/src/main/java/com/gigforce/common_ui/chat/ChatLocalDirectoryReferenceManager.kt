package com.gigforce.common_ui.chat

import android.os.Environment
import com.gigforce.common_ui.chat.ChatConstants
import java.io.File

class ChatLocalDirectoryReferenceManager {

    private val refToGigForceAttachmentDirectory: File  by lazy {
        Environment.getExternalStoragePublicDirectory(ChatConstants.DIRECTORY_APP_DATA_ROOT)!!
    }

     val imagesDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)

     val videosDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)

     val documentsDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)
}