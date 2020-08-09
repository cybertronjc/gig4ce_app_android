package com.gigforce.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareFile(file: File?, ctx: Context, mimeType: String) {
    file?.let {
        val uri: Uri = FileProvider.getUriForFile(
            ctx,
            ctx.packageName + ".provider",
            it
        )
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            ctx.startActivity(Intent.createChooser(intent, "Share Certificate"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

}

fun storeImage(
    bm: Bitmap?,
    fileName: String,
    dirPath: String
): String {
    val dir = File(dirPath)
    if (!dir.exists()) dir.mkdirs()
    val file = File(dirPath, fileName)
    return try {
        val fOut = FileOutputStream(file)
        bm?.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()
        file.absolutePath
    } catch (e: Exception) {
        ""
    }
}
