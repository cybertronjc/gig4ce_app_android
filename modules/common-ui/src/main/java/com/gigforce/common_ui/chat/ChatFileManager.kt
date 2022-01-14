package com.gigforce.common_ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.gigforce.common_ui.MimeTypes
import com.gigforce.core.date.DateHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val gigforceDirectory: File by lazy {

        File(context.filesDir, ChatConstants.DIRECTORY_APP_DATA_ROOT).apply {

            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    val imageFilesDirectory: File by lazy {
        File(gigforceDirectory, ChatConstants.DIRECTORY_IMAGES).apply {

            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    val videoFilesDirectory: File by lazy {
        File(gigforceDirectory, ChatConstants.DIRECTORY_VIDEOS).apply {

            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    val documentFilesDirectory: File by lazy {
        File(gigforceDirectory, ChatConstants.DIRECTORY_DOCUMENTS).apply {

            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    val otherFilesDirectory: File by lazy {
        File(gigforceDirectory, ChatConstants.DIRECTORY_OTHERS).apply {

            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    val audioFilesDirectory: File by lazy {
        File(gigforceDirectory, ChatConstants.DIRECTORY_AUDIOS).apply {
            if (!this.exists()) {
                mkdirs()
            }
        }
    }


    fun createImageFile(
        mimeType: String = MimeTypes.PNG
    ): Uri {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val newFileName = "IMG-${DateHelper.getFullDateTimeStamp()}.$extension"

        val file = File(
            imageFilesDirectory,
            newFileName
        )
        return file.toUri()
    }

}