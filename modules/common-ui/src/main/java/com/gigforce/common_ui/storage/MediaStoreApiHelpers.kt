package com.gigforce.common_ui.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.date.DateUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object MediaStoreApiHelpers {

    fun saveImageToGallery(
        context: Context,
        uri: Uri
    ){
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val newFileName = if(mimeType != null){
            val extension = getExtensionFromMimeType(mimeType)
            "IMG-${DateUtil.getFullDateTimeStamp()}.$extension"
        } else{
            "IMG-${DateUtil.getFullDateTimeStamp()}"
        }

        saveImageToGallery(
            context,
            uri,
            newFileName
        )
    }

    fun saveImageToGallery(
        context: Context,
        uri: Uri,
        fileName: String
    ) {

        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val sanitisedFileName = fixFileNameAddFileExtensionIfNotExist(fileName,mimeType)

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
            saveFileToMediaStorageAboveAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DCIM,
                subDirectory = "Gigforce/Images",
                mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                fileName = sanitisedFileName
            )
        } else {
            saveFileToMediaStorageBelowAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DCIM,
                subDirectory = "Gigforce/Images",
                fileName = sanitisedFileName
            )
        }
    }

    fun saveVideoToGallery(
        context: Context,
        uri: Uri
    ){
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val newFileName = if(mimeType != null){
            val extension = getExtensionFromMimeType(mimeType)
            "VID-${DateUtil.getFullDateTimeStamp()}.$extension"
        } else{
            "VID-${DateUtil.getFullDateTimeStamp()}"
        }

        saveVideoToGallery(
            context,
            uri,
            newFileName
        )
    }

    fun saveVideoToGallery(
        context: Context,
        uri: Uri,
        fileName : String
    ) {
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val sanitisedFileName = fixFileNameAddFileExtensionIfNotExist(fileName,mimeType)

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
            saveFileToMediaStorageAboveAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DCIM,
                subDirectory = "Gigforce/Videos",
                mediaContentUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                fileName = sanitisedFileName
            )
        } else {
            saveFileToMediaStorageBelowAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DCIM,
                subDirectory = "Gigforce/Videos",
                fileName = sanitisedFileName
            )
        }
    }

    fun saveDocumentToDownloads(
        context: Context,
        uri: Uri
    ){
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val newFileName = if(mimeType != null){
            val extension = getExtensionFromMimeType(mimeType)
            "DOC-${DateUtil.getFullDateTimeStamp()}.$extension"
        } else{
            "DOC-${DateUtil.getFullDateTimeStamp()}"
        }

        saveDocumentToDownloads(
            context,
            uri,
            newFileName
        )
    }

    fun saveDocumentToDownloads(
        context: Context,
        uri: Uri,
        fileName: String
    ) {
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, uri)
        val sanitisedFileName = fixFileNameAddFileExtensionIfNotExist(fileName,mimeType)

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {
            saveFileToMediaStorageAboveAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DOWNLOADS,
                subDirectory = "Gigforce Documents",
                mediaContentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                fileName = sanitisedFileName
            )
        } else {
            saveFileToMediaStorageBelowAndroidQ(
                context = context,
                file = uri,
                directory = Environment.DIRECTORY_DOWNLOADS,
                subDirectory = "Gigforce Documents",
                fileName = sanitisedFileName
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileToMediaStorageAboveAndroidQ(
        context: Context,
        file: Uri,
        directory: String,
        subDirectory: String?,
        fileName : String?,
        mediaContentUri: Uri
    ) {
        val finalFileName = fileName ?: ImageMetaDataHelpers.getImageName(context, file)
        val mimeType = ImageMetaDataHelpers.getImageMimeType(context, file)

        val sanitisedFileName = fixFileNameAddFileExtensionIfNotExist(
            finalFileName,
            mimeType
        )

        val imageOutStream: OutputStream
        val imageInputStream: InputStream = context.contentResolver.openInputStream(file) ?: return

        val fullPath = prepareFullPathInMediaFolder(
            directory,
            subDirectory
        )

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, sanitisedFileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, fullPath)
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }

        context.contentResolver.run {
            val uri = insert(
                mediaContentUri,
                values
            ) ?: return
            imageOutStream = openOutputStream(uri) ?: return
        }

        imageOutStream.use {

            val buf = ByteArray(8192)
            var length: Int
            while (imageInputStream.read(buf).also { length = it } > 0) {
                it.write(
                    buf,
                    0,
                    length
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun saveFileToMediaStorageBelowAndroidQ(
        context: Context,
        file: Uri,
        directory: String,
        subDirectory: String?,
        fileName: String
    ) {
        val imageOutStream: OutputStream
        val imageInputStream: InputStream = context.contentResolver.openInputStream(file)
            ?: throw Exception("Unable to open input stream to file")

        val directoryPath = Environment.getExternalStoragePublicDirectory(directory)

        val subDirectories = subDirectory?.split("/")
        if (subDirectories.isNullOrEmpty()) {

            val image = File(directoryPath, fileName)
            imageOutStream = FileOutputStream(image)
        } else {
            var finalDirectory: File = directoryPath
            subDirectories.forEach {
                finalDirectory = File(finalDirectory, it)
            }
            finalDirectory.mkdirs()

            val image = File(directoryPath, fileName)
            imageOutStream = FileOutputStream(image)
        }

        imageOutStream.use {

            val buf = ByteArray(8192)
            var length: Int
            while (imageInputStream.read(buf).also { length = it } > 0) {
                it.write(
                    buf,
                    0,
                    length
                )
            }
        }
    }


    private fun prepareFullPathInMediaFolder(directory: String, subDirectory: String?): String {

        return if (subDirectory != null) {
            "$directory/$subDirectory"
        } else {
            directory
        }
    }

    private fun fixFileNameAddFileExtensionIfNotExist(
        fileName: String,
        mimeType: String?
    ): String {
        require(fileName.isNotBlank()) { "file path should not be empty" }

        val fileNameSanitised = fileName.toLowerCase()
        val extension = if (mimeType == null) null else getExtensionFromMimeType(mimeType)
        return if (extension != null) {

            if (fileNameSanitised.endsWith(extension)) {
                fileNameSanitised
            } else {

                if (fileNameSanitised.endsWith(".")) {
                    fileNameSanitised + extension
                } else {
                    "$fileNameSanitised.$extension"
                }
            }
        } else {
            fileName
        }
    }

    private fun getExtensionFromMimeType(
        mimeType: String
    ): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}