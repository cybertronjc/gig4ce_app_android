package com.gigforce.common_ui.metaDataHelper

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.gigforce.common_ui.chat.models.VideoInfo
import com.gigforce.core.image.ImageUtils

object ImageMetaDataHelpers {

    fun getImageMetaData(
        context: Context,
        image: Uri
    ): ImageMetaData {
        val size = getImageSize(context, image)
        val aspectRatio = getAspectRatio(size)
        val imageLength = getImageLength(context, image)
        val imageName = getImageName(context, image)
        val mimeType = getImageMimeType(context, image)
        val imageExtension = getImageExtension(mimeType)
        val thumbnail = getImageThumbnail(context, image, size)

        return ImageMetaData(
            size = size,
            aspectRatio = aspectRatio,
            length = imageLength,
            name = imageName,
            mimeType = mimeType,
            extension = imageExtension,
            thumbnail = thumbnail
        )
    }

    private fun getImageThumbnail(
        context: Context,
        image: Uri,
        size: ImageSize
    ): Bitmap? {
        var targetHeight = 96
        var targetWidth = 96

        if (size.height != 0) {
            val aspectRatio = getAspectRatio(size)

            if (size.height > size.width) {
                targetWidth = (targetWidth * aspectRatio).toInt()
            } else {
                targetHeight = (targetHeight * aspectRatio).toInt()
            }
        }
        return ImageUtils.resizeBitmap(context, image, targetWidth, targetHeight)
    }

    fun getImageSize(
        context: Context,
        image: Uri
    ): ImageSize {
        val bitmapFactoryOptions = BitmapFactory.Options()
            .apply { inJustDecodeBounds = true }

        BitmapFactory.decodeStream(
            context.applicationContext.contentResolver.openInputStream(image),
            null,
            bitmapFactoryOptions
        )

        return ImageSize(
            bitmapFactoryOptions.outWidth,
            bitmapFactoryOptions.outHeight,
        )
    }

    fun getAspectRatio(
        context: Context,
        image: Uri
    ): Float {
        val imageSize = getImageSize(context, image)
        return if (imageSize.height == 0) {
            0.0f
        } else {
            imageSize.width / imageSize.height.toFloat()
        }
    }

    fun getAspectRatio(
        imageSize: ImageSize
    ): Float {
        return if (imageSize.height == 0) {
            0.0f
        } else {
            imageSize.width / imageSize.height.toFloat()
        }
    }

    @SuppressLint("Recycle")
    fun getImageLength(
        context: Context,
        image: Uri
    ): Long {

        var imageSize = 0L
        context.applicationContext.contentResolver.query(
            image,
            null,
            null,
            null,
            null
        )?.let {

            if (it.count != 0) {

                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                it.moveToFirst()
                it.getString(nameIndex)
                imageSize = it.getLong(sizeIndex)
            }

            closeCursorQuietly(it)
        }

        return imageSize
    }

    @SuppressLint("Recycle")
    fun getImageName(
        context: Context,
        image: Uri
    ): String {
        var imageName = ""


        if (ContentResolver.SCHEME_FILE == image.scheme) {
            return image.toFile().name
        } else if (ContentResolver.SCHEME_CONTENT == image.scheme) {

            context.applicationContext.contentResolver.query(
                image,
                null,
                null,
                null,
                null
            )?.let {

                if (it.count != 0) {

                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    imageName = it.getString(nameIndex)
                }

                closeCursorQuietly(it)
            }
        }
        return imageName
    }

    fun getImageName(
        cursor: Cursor
    ): String {
        var imageName = ""
        if (cursor.count != 0) {

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            imageName = cursor.getString(nameIndex)
        }
        return imageName
    }

    fun getImageMimeType(
        context: Context,
        image: Uri
    ): String? = context.applicationContext.contentResolver.getType(image)

    fun getImageExtension(
        context: Context,
        image: Uri
    ): String? {
        val mimeType = getImageMimeType(context, image) ?: return null
        return getImageExtension(mimeType)
    }

    fun getImageExtension(
        mimeType: String?
    ): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun getVideoInfo(
        content: Context,
        uri: Uri
    ): VideoInfo {

        val videoName = getImageName(content, uri)
        val videoSize = getImageLength(content, uri)
        val videoDuration = getVideoDuration(content, uri)
        val videoThumbnail = getVideoThumbnail(content,uri)

        return VideoInfo(
            name = videoName,
            size = videoSize,
            duration = videoDuration,
            thumbnail =videoThumbnail
        )
    }

    private fun getVideoDuration(
        content: Context,
        uri: Uri
    ): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(content, uri)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLong() ?: 0L
        } catch (e: Exception) {
            Log.e("FileMetaDataExtractor", "Error while fetching video length", e)
            0L
        }
    }

    private fun getVideoThumbnail(
        context: Context,
        uri: Uri
    ): Bitmap? {

        val mMMR = MediaMetadataRetriever()
        mMMR.setDataSource(context, uri)
        val thumbnail: Bitmap? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                mMMR.getScaledFrameAtTime(
                    -1,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    196,
                    196
                )
            } else {
                try {
                    val bigThumbnail = mMMR.frameAtTime
                    val smallThumbnail = ThumbnailUtils.extractThumbnail(bigThumbnail, 196, 196)

                    if (!bigThumbnail!!.isRecycled)
                        bigThumbnail.recycle()

                    smallThumbnail
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        mMMR.release()
        return thumbnail
    }

    fun closeCursorQuietly(cursor: Cursor) {
        try {
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}