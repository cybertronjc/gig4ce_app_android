package com.gigforce.core.documentFileHelper

import android.net.Uri

open class DocumentTreeFileManager constructor(
    private val documentPrefHelper: DocumentPrefHelper
){

    fun hasStorageTreeAccess() : Boolean{
        return documentPrefHelper.documentUriSaved()
    }

    fun doesFileExist(
        relativePath : String? = null,
        filePath : String
    ){

    }

    fun createFile(
        relativePath : String,
        fileNameWithExtension : String,
        mimeType : String
    ) {

    }
}