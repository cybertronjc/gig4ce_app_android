package com.gigforce.core.file

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import okhttp3.ResponseBody
import java.io.*


object FileUtils {

    fun writeResponseBodyToDisk(body: ResponseBody, destFile: File): Boolean {
        return try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("DownloadPath", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }

    fun isVirtualFile(context: Context, uri: Uri): Boolean {

        if (!DocumentsContract.isDocumentUri(context, uri)) {
            return false
        }

        val cursor: Cursor = context.getContentResolver().query(
            uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null, null, null
        ) ?: return false

        var flags = 0
        if (cursor.moveToFirst()) {
            flags = cursor.getInt(0)
        }
        cursor.close()

        return flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
    }

    @Throws(IOException::class)
    fun getInputStreamForVirtualFile(
        context: Context,
        uri: Uri,
        mimeTypeFilter: String?
    ): InputStream? {
        val resolver = context.contentResolver
        val openableMimeTypes = resolver.getStreamTypes(
            uri,
            mimeTypeFilter!!
        )
        if (openableMimeTypes == null || openableMimeTypes.isEmpty()) {
            throw FileNotFoundException()
        }
        return resolver
            .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
            ?.createInputStream()
    }

    private fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun copyFile(
        context: Context,
        name: String,
        sourceUri: Uri,
        destinationFile: File
    ): Boolean {
        val bis: BufferedInputStream
        var bos: BufferedOutputStream? = null
        var input: InputStream?
        var hasError = false
        try {
            input = if (isVirtualFile(context, sourceUri)) {
                getInputStreamForVirtualFile(context, sourceUri, getMimeType(name))
            } else {
                context.contentResolver.openInputStream(sourceUri)
            }

            val originalsize = input!!.available()
            bis = BufferedInputStream(input)
            bos = BufferedOutputStream(FileOutputStream(destinationFile))
            val buf = ByteArray(originalsize)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)

        } catch (e: Exception) {
            e.printStackTrace()
            hasError = true
        } finally {
            try {
                if (bos != null) {
                    bos.flush()
                    bos.close()
                }
            } catch (ignored: Exception) {
            }
        }
        return !hasError
    }
}