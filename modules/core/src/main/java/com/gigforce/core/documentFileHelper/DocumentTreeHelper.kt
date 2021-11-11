package com.gigforce.core.documentFileHelper

import android.net.Uri

object DocumentTreeHelper {

    private const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"

    fun isRootUri(
        uri : Uri
    ) : Boolean{
        val path = uri.path ?: return false
        return uri.isExternalStorageDocument && path.indexOf(':') == path.length - 1
    }

    val Uri.isExternalStorageDocument: Boolean
        get() = authority == EXTERNAL_STORAGE_AUTHORITY
}