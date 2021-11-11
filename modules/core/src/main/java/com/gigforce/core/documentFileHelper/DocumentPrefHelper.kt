package com.gigforce.core.documentFileHelper

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DocumentPrefHelper @Inject constructor(
    @Named("session_independent_pref") private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val USER_SELECTED_DOCUMENT_URI = "crc2e4a5sz666zws"
    }

    fun saveReceivedDocumentTreeUri(
        uri: Uri
    ) = sharedPreferences.edit {
        putString(USER_SELECTED_DOCUMENT_URI, uri.toString())
    }

    /**
     * Gives uri to Gigforce folder present in Shared storage
     */
    fun getSavedDocumentTreeUri(): Uri? {

        val uriSaved = sharedPreferences.getString(USER_SELECTED_DOCUMENT_URI, null) ?: return null
        return uriSaved.toUri()
    }

    fun documentUriSaved(): Boolean {
        return getSavedDocumentTreeUri() != null
    }
}