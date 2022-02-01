package com.gigforce.common_image_picker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtility {

    fun getImageInfo(
        file: File
    ): BitmapFactory.Options {

        val bitmapFactoryOptions = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(
            file.absolutePath,
            bitmapFactoryOptions
        )

        return bitmapFactoryOptions
    }

    fun loadRotateAndSaveImage(
        context: Context,
        actualFile: File
    ): File? {
        val bitmap = BitmapFactory.decodeFile(actualFile.absolutePath) ?: return null

        val matrix = Matrix().apply {
            postRotate(90.0f)
        }
        val rotatedImage = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        recycleBitmap(bitmap)
        return bitmapToFile(
            context,
            rotatedImage,
            "temp_rotated_${System.currentTimeMillis()}.jpg"
        )
    }

    private fun bitmapToFile(
        context: Context,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(context.filesDir, fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos) // YOU can also save it in JPEG
            recycleBitmap(bitmap)

            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        } finally {
            recycleBitmap(bitmap)
        }
    }

    private fun recycleBitmap(
        bitmap: Bitmap
    ) {

        try {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}