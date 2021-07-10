package com.gigforce.common_ui.metaDataHelper

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
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

    fun closeCursorQuietly(cursor: Cursor) {
        try {
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}