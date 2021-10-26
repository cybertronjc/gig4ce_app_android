package com.gigforce.modules.feature_chat.filemanager

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.core.date.DateHelper
import com.gigforce.core.documentFileHelper.DocumentPrefHelper
import com.gigforce.core.documentFileHelper.DocumentTreeFileManager

class ChatFileManager(
    private val context: Context,
    private val documentPrefHelper: DocumentPrefHelper
) : DocumentTreeFileManager(
    documentPrefHelper = documentPrefHelper
) {

    private val gigforceDirectory: DocumentFile by lazy {
        val rootTreeUri = documentPrefHelper.getSavedDocumentTreeUri()
            ?: throw IllegalStateException("root tree uri not saved")
        DocumentFile.fromTreeUri(context, rootTreeUri) ?: throw IllegalStateException("unable to get root tree")
    }

    private val imageFilesDirectory: DocumentFile by lazy {
        var gigforceImagesDirectory = gigforceDirectory.findFile(ChatConstants.DIRECTORY_IMAGES)

        if (gigforceImagesDirectory == null) {
            gigforceImagesDirectory = gigforceDirectory.createDirectory(
                ChatConstants.DIRECTORY_IMAGES
            )
        }

        gigforceImagesDirectory!!
    }

    private val videoFilesDirectory: DocumentFile by lazy {
        var gigforceVideosDirectory = gigforceDirectory.findFile(ChatConstants.DIRECTORY_VIDEOS)

        if (gigforceVideosDirectory == null) {
            gigforceVideosDirectory = gigforceDirectory.createDirectory(
                ChatConstants.DIRECTORY_VIDEOS
            )
        }

        gigforceVideosDirectory!!
    }

    private val documentFilesDirectory: DocumentFile by lazy {
        var gigforceDocumentsDirectory = gigforceDirectory.findFile(ChatConstants.DIRECTORY_DOCUMENTS)

        if (gigforceDocumentsDirectory == null) {
            gigforceDocumentsDirectory = gigforceDirectory.createDirectory(
                ChatConstants.DIRECTORY_DOCUMENTS
            )
        }

        gigforceDocumentsDirectory!!
    }

    private val otherFilesDirectory: DocumentFile by lazy {
        var gigforceDocumentsDirectory = gigforceDirectory.findFile(ChatConstants.DIRECTORY_OTHERS)

        if (gigforceDocumentsDirectory == null) {
            gigforceDocumentsDirectory = gigforceDirectory.createDirectory(
                ChatConstants.DIRECTORY_OTHERS
            )
        }

        gigforceDocumentsDirectory!!
    }


    fun checkIfMediaFileExistElseReturnNull(
        type: ChatFilesTypes,
        fileName: String
    ) = getMediaDirectory(type).findFile(fileName)?.uri


    private fun getMediaDirectory(
        type: ChatFilesTypes
    ): DocumentFile = when (type) {
        ChatFilesTypes.IMAGE -> imageFilesDirectory
        ChatFilesTypes.VIDEO -> videoFilesDirectory
        ChatFilesTypes.DOCUMENT -> documentFilesDirectory
        ChatFilesTypes.OTHER -> otherFilesDirectory
    }

    fun createImageFile(
        mimeType: String = MimeTypes.PNG
    ): Uri {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val newFileName = "IMG-${DateHelper.getFullDateTimeStamp()}.$extension"
       return imageFilesDirectory.createFile(
            mimeType,
            newFileName
        )?.uri ?: throw Exception("ChatFileManager : unable to create image file")
    }

}