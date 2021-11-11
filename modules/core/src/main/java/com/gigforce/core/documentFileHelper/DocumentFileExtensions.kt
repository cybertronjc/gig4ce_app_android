package com.gigforce.core.documentFileHelper

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import java.io.*

val Uri.isTreeDocumentFile: Boolean
    get() = path?.startsWith("/tree/") == true

val Uri.isRawFile: Boolean
    get() = scheme == ContentResolver.SCHEME_FILE

fun Uri.openOutputStream(
    context: Context,
    append: Boolean = true
): OutputStream? {
    return try {
        if (isRawFile) {
            FileOutputStream(File(path ?: return null), append)
        } else {
            context.contentResolver.openOutputStream(this, if (append && isTreeDocumentFile) "wa" else "w")
        }
    } catch (e: IOException) {
        null
    }
}

@WorkerThread
fun Uri.openInputStream(context: Context): InputStream? {
    return try {
        if (isRawFile) {
            // handle file from external storage
            FileInputStream(File(path ?: return null))
        } else {
            context.contentResolver.openInputStream(this)
        }
    } catch (e: IOException) {
        null
    }
}

fun DocumentFile.openOutputStream(
    context: Context,
    append: Boolean = true
) : OutputStream? = uri.openOutputStream(
    context = context,
    append = append
)

fun DocumentFile.openOutputStreamOrThrow(
    context: Context,
    append: Boolean = true
) : OutputStream = uri.openOutputStream(
    context = context,
    append = append
) ?: throw Exception("unable to open outstream to uri : ${uri.toString()}")