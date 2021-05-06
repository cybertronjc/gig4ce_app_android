package com.gigforce.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtils {

     fun resizeBitmap(photoPath: String, targetW: Int, targetH: Int): Bitmap? {
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPath, bmOptions)
        val photoW: Int = bmOptions.outWidth
        val photoH: Int = bmOptions.outHeight
        var scaleFactor = 1
        if (targetW > 0 || targetH > 0) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        }
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true //Deprecated API 21
        return BitmapFactory.decodeFile(photoPath, bmOptions)
    }

    fun convertToByteArray(thumbnail: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        thumbnail.recycle()
        return byteArray
    }

    fun createThumbnail(){

    }

}