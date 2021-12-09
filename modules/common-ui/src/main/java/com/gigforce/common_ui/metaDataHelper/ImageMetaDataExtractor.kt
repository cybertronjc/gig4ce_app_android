package com.gigforce.common_ui.metaDataHelper

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.webkit.MimeTypeMap

import android.content.ContentResolver
import java.io.File


@Singleton
class FileMetaDataExtractor @Inject constructor(
    @ApplicationContext private val context : Context
) {

    fun getMimeTypeOrThrow(
        uri: Uri
    ) : String{

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else if (uri.scheme.equals(ContentResolver.SCHEME_FILE)){
            //If scheme is a File
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(uri.path).extension)
        } else{
            null
        }

        return extension ?: throw Exception("unable to fetch mime type")
    }
}